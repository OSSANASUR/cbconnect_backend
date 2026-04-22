-- ============================================================
--  V24 — Module Expertises : affectation + champs manquants
-- ============================================================

-- 1. Expert : email + telephone
ALTER TABLE expert
    ADD COLUMN IF NOT EXISTS email      VARCHAR(200),
    ADD COLUMN IF NOT EXISTS telephone  VARCHAR(30);

-- 2. ExpertiseMaterielle : victime + type_expertise + champs véhicule
ALTER TABLE expertise_materielle
    ADD COLUMN IF NOT EXISTS type_expertise        VARCHAR(30),
    ADD COLUMN IF NOT EXISTS victime_id            INT REFERENCES victime(historique_id),
    ADD COLUMN IF NOT EXISTS marque_vehicule       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS modele_vehicule       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS immatriculation       VARCHAR(30),
    ADD COLUMN IF NOT EXISTS annee_vehicule        INT,
    ADD COLUMN IF NOT EXISTS nature_dommages       TEXT,
    ADD COLUMN IF NOT EXISTS est_vei               BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS valeur_vehicule_neuf  NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS valeur_venal          NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS valeur_reparable      NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS observations          TEXT;

-- 3. Table affectation_expert
--    Colonnes alignées sur InternalHistorique :
--    historique_id, created_at, updated_at, deleted_at,
--    created_by, updated_by, deleted_by,
--    libelle, active_data, parent_code_id, deleted_data, from_table, excel
CREATE TABLE IF NOT EXISTS affectation_expert (
    -- ── Clé primaire InternalHistorique ─────────────────────────
    historique_id       SERIAL PRIMARY KEY,

    -- ── Audit InternalHistorique ─────────────────────────────────
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    deleted_by          VARCHAR(255),
    libelle             VARCHAR(255),
    active_data         BOOLEAN NOT NULL DEFAULT TRUE,
    parent_code_id      VARCHAR(255),
    deleted_data        BOOLEAN NOT NULL DEFAULT FALSE,
    from_table          VARCHAR(50)      DEFAULT 'AFFECTATION_EXPERT',
    excel               BOOLEAN NOT NULL DEFAULT FALSE,

    -- ── Champs métier ────────────────────────────────────────────
    affectation_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    expert_id               INT  NOT NULL REFERENCES expert(historique_id),
    victime_id              INT  NOT NULL REFERENCES victime(historique_id),
    sinistre_id             INT  NOT NULL REFERENCES sinistre(historique_id),
    type_expertise          VARCHAR(30) NOT NULL,
    date_affectation        DATE NOT NULL DEFAULT CURRENT_DATE,
    date_limite_rapport     DATE,
    statut                  VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',

    -- ── Courriers générés ────────────────────────────────────────
    courrier_mission_id     INT REFERENCES courrier(historique_id),
    courrier_victime_id     INT REFERENCES courrier(historique_id),
    mail_expert_envoye      BOOLEAN NOT NULL DEFAULT FALSE,
    mail_victime_envoye     BOOLEAN NOT NULL DEFAULT FALSE,
    observations            TEXT,

    -- ── Contrainte métier ────────────────────────────────────────
    UNIQUE (expert_id, victime_id, type_expertise)
);

COMMENT ON TABLE affectation_expert IS
    'Affectation d''un expert à une victime pour un type d''expertise donné.
     Génère automatiquement 2 courriers : note de mission (expert) + lettre de prévenance (victime).';