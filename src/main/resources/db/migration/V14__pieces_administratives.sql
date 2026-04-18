-- ============================================================
--  V13 — Pièces administratives par type de dommage
--        + personne morale sur Assure
-- ============================================================

-- ── 1. Paramétrage des types de pièces ───────────────────────────
CREATE TABLE IF NOT EXISTS type_piece_administrative (
    historique_id    SERIAL PRIMARY KEY,
    tracking_id      UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    libelle          VARCHAR(150) NOT NULL,
    -- NULL = COMMUN (applicable à tous les types de dommage)
    -- 'CORPOREL' | 'MATERIEL' | 'MIXTE' = spécifique
    type_dommage     VARCHAR(20),
    obligatoire      BOOLEAN     NOT NULL DEFAULT TRUE,
    ordre            INT         NOT NULL DEFAULT 0,
    actif            BOOLEAN     NOT NULL DEFAULT TRUE,
    -- Audit OssanAuth
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ,
    deleted_at       TIMESTAMPTZ,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    deleted_by       VARCHAR(100),
    active_data      BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_data     BOOLEAN     NOT NULL DEFAULT FALSE,
    parent_code_id   VARCHAR(100),
    from_table       VARCHAR(100)
);

COMMENT ON COLUMN type_piece_administrative.type_dommage IS
    'NULL = pièce commune à tous les dossiers. CORPOREL|MATERIEL|MIXTE = pièce spécifique.';

-- ── 2. Pièces par dossier de réclamation (liaison dossier ↔ pièce ↔ doc GED) ──
CREATE TABLE IF NOT EXISTS piece_dossier_reclamation (
    historique_id             SERIAL PRIMARY KEY,
    tracking_id               UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    dossier_reclamation_id    INT         NOT NULL REFERENCES dossier_reclamation(historique_id),
    type_piece_id             INT         NOT NULL REFERENCES type_piece_administrative(historique_id),
    -- Lien vers le document GED (NULL = pièce non encore reçue)
    paperless_document_id     INT         REFERENCES paperless_document(historique_id),
    statut                    VARCHAR(20) NOT NULL DEFAULT 'ATTENDUE',
    -- ATTENDUE | RECUE | REJETEE
    date_reception            DATE,
    notes                     TEXT,
    -- Audit OssanAuth
    created_at                TIMESTAMPTZ DEFAULT NOW(),
    updated_at                TIMESTAMPTZ,
    deleted_at                TIMESTAMPTZ,
    created_by                VARCHAR(100),
    updated_by                VARCHAR(100),
    deleted_by                VARCHAR(100),
    active_data               BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_data              BOOLEAN     NOT NULL DEFAULT FALSE,
    parent_code_id            VARCHAR(100),
    from_table                VARCHAR(100),
    -- Unicité : une seule entrée par type de pièce par dossier
    UNIQUE (dossier_reclamation_id, type_piece_id)
);

COMMENT ON COLUMN piece_dossier_reclamation.statut IS
    'ATTENDUE = non reçue | RECUE = document GED associé | REJETEE = document refusé';

-- ── 3. Assure — personne morale ──────────────────────────────────
ALTER TABLE assure
    ADD COLUMN IF NOT EXISTS est_personne_morale BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN assure.est_personne_morale IS
    'TRUE = personne morale (entreprise, société). FALSE = personne physique.';

-- ── 4. Vue maturité dossier ──────────────────────────────────────
CREATE OR REPLACE VIEW v_maturite_dossier AS
SELECT
    dr.historique_id                                            AS dossier_id,
    dr.numero_dossier,
    s.type_dommage,
    -- Total pièces obligatoires applicables (COMMUN + type_dommage du sinistre)
    COUNT(tpa.historique_id)                                    AS nb_pieces_requises,
    -- Pièces effectivement reçues
    COUNT(pdr.historique_id) FILTER (WHERE pdr.statut = 'RECUE') AS nb_pieces_recues,
    -- Mûr = au moins 1 pièce requise ET toutes les obligatoires reçues
    (COUNT(tpa.historique_id) > 0
     AND COUNT(tpa.historique_id) = COUNT(pdr.historique_id) FILTER (WHERE pdr.statut = 'RECUE'))
                                                                AS est_mur
FROM dossier_reclamation dr
JOIN sinistre s ON s.historique_id = dr.sinistre_id
-- Pièces applicables : COMMUN (type_dommage IS NULL) ou matching type_dommage
JOIN type_piece_administrative tpa
    ON tpa.obligatoire = TRUE
    AND tpa.actif = TRUE
    AND tpa.deleted_data = FALSE
    AND (tpa.type_dommage IS NULL OR tpa.type_dommage = s.type_dommage)
LEFT JOIN piece_dossier_reclamation pdr
    ON pdr.dossier_reclamation_id = dr.historique_id
    AND pdr.type_piece_id = tpa.historique_id
    AND pdr.deleted_data = FALSE
WHERE dr.deleted_data = FALSE
  AND s.deleted_data = FALSE
GROUP BY dr.historique_id, dr.numero_dossier, s.type_dommage;

-- ── 5. Seed — pièces communes et spécifiques de base ────────────
INSERT INTO type_piece_administrative
    (libelle, type_dommage, obligatoire, ordre, actif, created_by, active_data, deleted_data)
VALUES
    -- Pièces COMMUNES (type_dommage = NULL)
    ('Pièce d''identité / CNI',               NULL,        TRUE,  1, TRUE, 'SYSTEM', TRUE, FALSE),
    ('PV de Police / Constat amiable',         NULL,        TRUE,  2, TRUE, 'SYSTEM', TRUE, FALSE),

    -- Pièces CORPOREL
    ('Certificat médical initial',             'CORPOREL',  TRUE,  1, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Rapport d''expertise médicale',          'CORPOREL',  TRUE,  2, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Factures médicales',                     'CORPOREL',  TRUE,  3, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Ordonnances médicales',                  'CORPOREL',  FALSE, 4, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Certificat de décès',                    'CORPOREL',  FALSE, 5, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Acte de naissance',                      'CORPOREL',  FALSE, 6, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Acte de mariage',                        'CORPOREL',  FALSE, 7, TRUE, 'SYSTEM', TRUE, FALSE),

    -- Pièces MATERIEL
    ('Carte grise du véhicule',                'MATERIEL',  TRUE,  1, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Rapport d''expertise automobile',        'MATERIEL',  TRUE,  2, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Factures de réparation',                 'MATERIEL',  TRUE,  3, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Photos des dommages matériels',          'MATERIEL',  FALSE, 4, TRUE, 'SYSTEM', TRUE, FALSE),

    -- MIXTE — regroupe les deux
    ('Certificat médical initial',             'MIXTE',     TRUE,  1, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Rapport d''expertise médicale',          'MIXTE',     TRUE,  2, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Factures médicales',                     'MIXTE',     TRUE,  3, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Carte grise du véhicule',                'MIXTE',     TRUE,  4, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Rapport d''expertise automobile',        'MIXTE',     TRUE,  5, TRUE, 'SYSTEM', TRUE, FALSE),
    ('Factures de réparation',                 'MIXTE',     TRUE,  6, TRUE, 'SYSTEM', TRUE, FALSE),

    -- Type générique "Autre" toujours disponible
    ('Autre document',                         NULL,        FALSE, 99, TRUE, 'SYSTEM', TRUE, FALSE)

ON CONFLICT DO NOTHING;
