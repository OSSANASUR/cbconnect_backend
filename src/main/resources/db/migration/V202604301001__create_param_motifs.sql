-- =============================================================================
-- V202604301001 — Création de la table param_motifs
--   Entité de paramétrage des motifs (REGLEMENT / ANNULATION / PREFINANCEMENT)
-- =============================================================================

CREATE TABLE param_motifs (
    -- Colonnes héritées d'InternalHistorique
    historique_id           SERIAL PRIMARY KEY,
    created_at              TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_by              VARCHAR(150),
    updated_by              VARCHAR(150),
    deleted_by              VARCHAR(150),
    libelle                 VARCHAR(255),
    active_data             BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_data            BOOLEAN NOT NULL DEFAULT FALSE,
    parent_code_id          VARCHAR(255),
    from_table              VARCHAR(50)  DEFAULT 'PARAM_MOTIF',
    excel                   BOOLEAN NOT NULL DEFAULT FALSE,

    -- Colonnes propres à ParamMotif
    param_motif_tracking_id UUID         NOT NULL UNIQUE,
    libelle_motif           VARCHAR(150) NOT NULL,
    type                    VARCHAR(30)  NOT NULL,
    actif                   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Index unique partiel : unicité (libelle_motif, type) sur les lignes actives uniquement
CREATE UNIQUE INDEX uk_param_motifs_libelle_type_active
    ON param_motifs(libelle_motif, type)
    WHERE active_data = TRUE AND deleted_data = FALSE;

-- Index de recherche par type + actif sur les lignes actives
CREATE INDEX idx_param_motifs_type_actif
    ON param_motifs(type, actif)
    WHERE active_data = TRUE AND deleted_data = FALSE;

-- =============================================================================
-- Seed initial des motifs de base
-- =============================================================================
INSERT INTO param_motifs (param_motif_tracking_id, libelle_motif, type, actif, active_data, deleted_data, excel, created_at, created_by, from_table) VALUES
    (gen_random_uuid(), 'Remboursement frais médicaux',           'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Indemnité provisionnelle',               'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Indemnité définitive',                   'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Honoraires expert médical',              'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Honoraires expert matériel',             'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Provision sur sinistre',                 'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Remboursement réparation matériel',      'REGLEMENT',     TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Erreur de saisie',                       'ANNULATION',    TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Chèque rejeté par la banque',            'ANNULATION',    TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Annulation à la demande du bénéficiaire','ANNULATION',    TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Avance sur sinistre',                    'PREFINANCEMENT',TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF'),
    (gen_random_uuid(), 'Préfinancement frais médicaux',          'PREFINANCEMENT',TRUE, TRUE, FALSE, FALSE, NOW(), 'SYSTEM', 'PARAM_MOTIF');
