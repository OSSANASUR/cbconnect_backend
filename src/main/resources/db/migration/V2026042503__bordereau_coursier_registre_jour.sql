-- ═══════════════════════════════════════════════════════════════════════════
-- V30 — Gestion courriers physiques : Bordereau coursier + Registre journalier
-- ═══════════════════════════════════════════════════════════════════════════

-- ─── 1. Rôle COURSIER ────────────────────────────────────────────────────────
INSERT INTO profil (profil_tracking_id, profil_nom, commentaire,
                    created_at, created_by, active_data, deleted_data, from_table)
SELECT gen_random_uuid(), 'COURSIER', 'Agent chargé du transport physique des courriers',
       NOW(), 'SYSTEM', true, false, 'PROFIL'
 WHERE NOT EXISTS (
    SELECT 1 FROM profil
     WHERE upper(profil_nom) = 'COURSIER' AND active_data = true AND deleted_data = false
 );

-- ─── 2. Table BORDEREAU_COURSIER ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bordereau_coursier (
    historique_id           SERIAL PRIMARY KEY,
    bordereau_tracking_id   UUID NOT NULL,
    numero_bordereau        VARCHAR(40) NOT NULL,

    -- Destinataire (homologue OU libre)
    destinataire_organisme_id BIGINT REFERENCES organisme(historique_id),
    destinataire_libre      VARCHAR(255),
    lieu_depart             VARCHAR(100) NOT NULL DEFAULT 'Lomé',

    -- Workflow dates (remplies progressivement)
    date_creation           TIMESTAMP NOT NULL DEFAULT NOW(),
    date_remise_coursier    TIMESTAMP,     -- sortie du bureau, remis au coursier
    date_remise_transporteur TIMESTAMP,    -- déposé à la poste / bus
    date_decharge_recue     TIMESTAMP,     -- retour physique signé du destinataire

    coursier_id             BIGINT REFERENCES utilisateur(historique_id),

    -- Transport
    transporteur            VARCHAR(30) NOT NULL,      -- POSTE|BUS|COURSIER_INTERNE|DHL|AUTRE
    nom_compagnie_bus       VARCHAR(100),
    reference_transporteur  VARCHAR(80),               -- ref facture poste, ticket bus
    montant_transporteur    DECIMAL(12,2),

    -- Workflow
    statut                  VARCHAR(30) NOT NULL DEFAULT 'BROUILLON',
                            -- BROUILLON|IMPRIME|REMIS_TRANSPORTEUR|DECHARGE_RECUE|RETOURNE

    -- Preuves GED
    decharge_ged_document_id INTEGER,                  -- scan bordereau signé retour
    facture_ged_document_id  INTEGER,                  -- scan facture poste/bus

    observations            TEXT,

    -- InternalHistorique
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    deleted_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    deleted_by              VARCHAR(255),
    libelle                 VARCHAR(255),
    active_data             BOOLEAN NOT NULL DEFAULT true,
    parent_code_id          VARCHAR(255),
    deleted_data            BOOLEAN NOT NULL DEFAULT false,
    from_table              VARCHAR(50) DEFAULT 'BORDEREAU_COURSIER',
    excel                   BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT ck_bordereau_destinataire CHECK (
        destinataire_organisme_id IS NOT NULL OR destinataire_libre IS NOT NULL
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_bordereau_numero_actif
    ON bordereau_coursier (numero_bordereau)
    WHERE active_data = true AND deleted_data = false;
CREATE INDEX IF NOT EXISTS idx_bordereau_tracking ON bordereau_coursier (bordereau_tracking_id);
CREATE INDEX IF NOT EXISTS idx_bordereau_statut   ON bordereau_coursier (statut);
CREATE INDEX IF NOT EXISTS idx_bordereau_dest     ON bordereau_coursier (destinataire_organisme_id);

-- ─── 3. Table REGISTRE_JOUR ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS registre_jour (
    historique_id         SERIAL PRIMARY KEY,
    registre_tracking_id  UUID NOT NULL,
    date_jour             DATE NOT NULL,
    type_registre         VARCHAR(10) NOT NULL,  -- ARRIVEE | DEPART

    statut                VARCHAR(15) NOT NULL DEFAULT 'OUVERT',
                          -- OUVERT | CLOS | VISE

    secretaire_id         BIGINT REFERENCES utilisateur(historique_id),

    -- Clôture (secrétaire)
    date_cloture          TIMESTAMP,
    clos_par              VARCHAR(255),

    -- Visa chef
    vise_par_id           BIGINT REFERENCES utilisateur(historique_id),
    date_visa             TIMESTAMP,
    commentaire_chef      TEXT,

    -- Scan registre physique signé (optionnel)
    scan_ged_document_id  INTEGER,

    -- InternalHistorique
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    deleted_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    deleted_by            VARCHAR(255),
    libelle               VARCHAR(255),
    active_data           BOOLEAN NOT NULL DEFAULT true,
    parent_code_id        VARCHAR(255),
    deleted_data          BOOLEAN NOT NULL DEFAULT false,
    from_table            VARCHAR(50) DEFAULT 'REGISTRE_JOUR',
    excel                 BOOLEAN NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_registre_date_type_actif
    ON registre_jour (date_jour, type_registre)
    WHERE active_data = true AND deleted_data = false;
CREATE INDEX IF NOT EXISTS idx_registre_statut ON registre_jour (statut);

-- ─── 4. Extension table COURRIER ─────────────────────────────────────────────
ALTER TABLE courrier ADD COLUMN IF NOT EXISTS bordereau_id BIGINT REFERENCES bordereau_coursier(historique_id);
ALTER TABLE courrier ADD COLUMN IF NOT EXISTS ordre_dans_bordereau INTEGER;
ALTER TABLE courrier ADD COLUMN IF NOT EXISTS numero_sinistre_homologue_ref VARCHAR(60);
ALTER TABLE courrier ADD COLUMN IF NOT EXISTS destinataire_organisme_id BIGINT REFERENCES organisme(historique_id);
ALTER TABLE courrier ADD COLUMN IF NOT EXISTS registre_jour_id BIGINT REFERENCES registre_jour(historique_id);
ALTER TABLE courrier ADD COLUMN IF NOT EXISTS service_destinataire_interne VARCHAR(100);

-- Étendre enum canal pour accepter POSTE/BUS comme sous-type physique
-- (canal reste MAIL|PHYSIQUE ; le détail transport est sur le bordereau)

CREATE INDEX IF NOT EXISTS idx_courrier_bordereau ON courrier (bordereau_id);
CREATE INDEX IF NOT EXISTS idx_courrier_registre  ON courrier (registre_jour_id);
CREATE INDEX IF NOT EXISTS idx_courrier_dest_org  ON courrier (destinataire_organisme_id);

-- ─── 5. Table junction COURRIER_DESTINATAIRE_INTERNE ─────────────────────────
-- Un courrier entrant peut être dispatché à N utilisateurs / services BNCB
CREATE TABLE IF NOT EXISTS courrier_destinataire_interne (
    id                  SERIAL PRIMARY KEY,
    courrier_id         BIGINT NOT NULL REFERENCES courrier(historique_id) ON DELETE CASCADE,
    utilisateur_id      BIGINT REFERENCES utilisateur(historique_id),
    service_libelle     VARCHAR(150),                -- "Rédaction", "Comptabilité", "Direction"…
    date_remise_interne TIMESTAMP,
    observations        TEXT,

    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255),

    CONSTRAINT ck_cdi_dest CHECK (utilisateur_id IS NOT NULL OR service_libelle IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_cdi_courrier     ON courrier_destinataire_interne (courrier_id);
CREATE INDEX IF NOT EXISTS idx_cdi_utilisateur  ON courrier_destinataire_interne (utilisateur_id);
