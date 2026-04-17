-- CBConnect V4 – Modules Sinistres, PV, Expertises, Réclamations

-- ── Entité Constat (référentiel) ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS entite_constat (
    historique_id  SERIAL PRIMARY KEY,
    entite_constat_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    nom            VARCHAR(200) NOT NULL,
    type           VARCHAR(15)  NOT NULL CHECK (type IN ('POLICE','GENDARMERIE','MIXTE')),
    localite       VARCHAR(100),
    code_postal    VARCHAR(20),
    actif          BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'ENTITE_CONSTAT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_entite_constat_tracking_actif ON entite_constat (entite_constat_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Assure ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS assure (
    historique_id  SERIAL PRIMARY KEY,
    assure_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    nom_assure     VARCHAR(100) NOT NULL,
    prenom_assure  VARCHAR(100),
    nom_complet    VARCHAR(200) NOT NULL,
    numero_police  VARCHAR(50),
    numero_attestation VARCHAR(20),
    numero_c_grise VARCHAR(50),
    proprietaire_vehicule VARCHAR(200),
    immatriculation VARCHAR(30),
    marque_vehicule VARCHAR(100),
    telephone      VARCHAR(30),
    adresse        TEXT,
    organisme_id   INTEGER REFERENCES organisme(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'ASSURE', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_assure_tracking_actif ON assure (assure_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Sinistre ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sinistre (
    historique_id  SERIAL PRIMARY KEY,
    sinistre_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_sinistre_local    VARCHAR(30) NOT NULL,
    numero_sinistre_manuel   VARCHAR(30),
    numero_sinistre_homologue VARCHAR(50),
    numero_sinistre_ecarte_brune VARCHAR(50),
    type_sinistre  VARCHAR(20) NOT NULL CHECK (type_sinistre IN ('SURVENU_TOGO','SURVENU_ETRANGER')),
    statut         VARCHAR(30) NOT NULL DEFAULT 'NOUVEAU',
    type_dommage   VARCHAR(10) NOT NULL CHECK (type_dommage IN ('CORPOREL','MATERIEL','MIXTE')),
    date_accident  DATE NOT NULL,
    date_declaration DATE NOT NULL DEFAULT CURRENT_DATE,
    lieu_accident  TEXT,
    agglomeration  BOOLEAN NOT NULL DEFAULT false,
    taux_rc        DECIMAL(5,2),
    position_rc    VARCHAR(15),
    est_prefinance BOOLEAN NOT NULL DEFAULT false,
    est_contentieux BOOLEAN NOT NULL DEFAULT false,
    niveau_juridiction VARCHAR(100),
    date_prochaine_audience DATE,
    paperless_dossier_id BIGINT,
    pays_gestionnaire_id INTEGER NOT NULL REFERENCES pays(historique_id),
    pays_emetteur_id INTEGER REFERENCES pays(historique_id),
    organisme_membre_id INTEGER REFERENCES organisme(historique_id),
    organisme_homologue_id INTEGER REFERENCES organisme(historique_id),
    assure_id      INTEGER NOT NULL REFERENCES assure(historique_id),
    redacteur_id   INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'SINISTRE', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_sinistre_tracking_actif ON sinistre (sinistre_tracking_id) WHERE active_data = true AND deleted_data = false;
CREATE UNIQUE INDEX uk_sinistre_numero_actif ON sinistre (numero_sinistre_local) WHERE active_data = true AND deleted_data = false;
CREATE INDEX idx_sinistre_statut ON sinistre (statut) WHERE active_data = true AND deleted_data = false;
CREATE INDEX idx_sinistre_date_declaration ON sinistre (date_declaration DESC);
COMMENT ON TABLE sinistre IS 'Machine d etat a 22 statuts. Jamais de DELETE SQL.';

-- ── Victime ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS victime (
    historique_id  SERIAL PRIMARY KEY,
    victime_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    nom            VARCHAR(100) NOT NULL,
    prenoms        VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    sexe           CHAR(1) NOT NULL CHECK (sexe IN ('M','F')),
    nationalite    VARCHAR(50),
    type_victime   VARCHAR(10) NOT NULL DEFAULT 'NEUTRE' CHECK (type_victime IN ('BLESSE','DECEDE','NEUTRE')),
    statut_victime VARCHAR(30) NOT NULL DEFAULT 'NEUTRE',
    statut_activite VARCHAR(20) NOT NULL,
    revenu_mensuel DECIMAL(15,2) NOT NULL DEFAULT 0,
    est_dcd_suite_blessures BOOLEAN NOT NULL DEFAULT false,
    date_deces     DATE,
    lien_deces_accident BOOLEAN NOT NULL DEFAULT false,
    paperless_correspondent_id INTEGER,
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    pays_residence_id INTEGER REFERENCES pays(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'VICTIME', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_victime_tracking_actif ON victime (victime_tracking_id) WHERE active_data = true AND deleted_data = false;
COMMENT ON COLUMN victime.statut_victime IS 'Statut individuel par victime. Distinct du statut global du SINISTRE.';

-- ── PV Sinistre ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pv_sinistre (
    historique_id  SERIAL PRIMARY KEY,
    pv_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_pv      VARCHAR(60) NOT NULL,
    sens_circulation VARCHAR(5) NOT NULL CHECK (sens_circulation IN ('ET','TE')),
    lieu_accident  TEXT NOT NULL,
    date_accident_pv DATE NOT NULL,
    date_reception_bncb DATE NOT NULL,
    provenance     TEXT,
    reference_sinistre_liee VARCHAR(50),
    a_circonstances VARCHAR(10) NOT NULL DEFAULT 'NEANT',
    a_auditions    VARCHAR(10) NOT NULL DEFAULT 'NEANT',
    a_croquis      VARCHAR(10) NOT NULL DEFAULT 'NEANT',
    est_complet    BOOLEAN NOT NULL DEFAULT false,
    remarques      TEXT,
    paperless_document_id INTEGER,
    entite_constat_id INTEGER NOT NULL REFERENCES entite_constat(historique_id),
    sinistre_id    INTEGER REFERENCES sinistre(historique_id),
    enregistre_par_id INTEGER NOT NULL REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PV_SINISTRE', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_pv_tracking_actif ON pv_sinistre (pv_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Expert ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS expert (
    historique_id  SERIAL PRIMARY KEY,
    expert_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    type_expert    VARCHAR(15) NOT NULL CHECK (type_expert IN ('MEDICAL','AUTOMOBILE')),
    nom_complet    VARCHAR(200) NOT NULL,
    specialite     VARCHAR(100),
    nif            VARCHAR(30),
    taux_retenue   DECIMAL(5,2),
    actif          BOOLEAN NOT NULL DEFAULT true,
    pays_id        INTEGER REFERENCES pays(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'EXPERT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_expert_tracking_actif ON expert (expert_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Expertise Medicale ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS expertise_medicale (
    historique_id  SERIAL PRIMARY KEY,
    expertise_med_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    type_expertise VARCHAR(25) NOT NULL,
    date_demande   DATE NOT NULL,
    date_rapport   DATE,
    date_consolidation DATE,
    taux_ipp       DECIMAL(5,2) NOT NULL DEFAULT 0,
    duree_itt_jours INTEGER NOT NULL DEFAULT 0,
    duree_itp_jours INTEGER NOT NULL DEFAULT 0,
    pretium_doloris VARCHAR(25) NOT NULL DEFAULT 'NEANT',
    prejudice_esthetique VARCHAR(25) NOT NULL DEFAULT 'NEANT',
    necessite_tierce_personne BOOLEAN NOT NULL DEFAULT false,
    honoraires     DECIMAL(15,2) NOT NULL DEFAULT 0,
    honoraires_contre_expertise DECIMAL(15,2) NOT NULL DEFAULT 0,
    paperless_document_id INTEGER,
    victime_id     INTEGER NOT NULL REFERENCES victime(historique_id),
    expert_id      INTEGER REFERENCES expert(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'EXPERTISE_MEDICALE', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_expertise_med_tracking_actif ON expertise_medicale (expertise_med_tracking_id) WHERE active_data = true AND deleted_data = false;
COMMENT ON COLUMN expertise_medicale.date_demande IS 'Declenche l alerte 20 jours si date_rapport NULL';

-- ── Expertise Materielle ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS expertise_materielle (
    historique_id  SERIAL PRIMARY KEY,
    expertise_ma_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    date_demande   DATE NOT NULL,
    date_rapport   DATE,
    montant_devis  DECIMAL(15,2),
    montant_dit_expert DECIMAL(15,2),
    honoraires     DECIMAL(15,2) NOT NULL DEFAULT 0,
    paperless_document_id INTEGER,
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    expert_id      INTEGER REFERENCES expert(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'EXPERTISE_MATERIELLE', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_expertise_ma_tracking_actif ON expertise_materielle (expertise_ma_tracking_id) WHERE active_data = true AND deleted_data = false;

-- Seed entités constat courantes (Togo)
INSERT INTO entite_constat (nom, type, localite, actif, created_at, created_by, active_data, deleted_data)
VALUES
  ('Brigade Territoriale de Lomé',         'GENDARMERIE', 'Lomé',     true, NOW(), 'SYSTEM', true, false),
  ('Brigade de Recherche et Investigation','GENDARMERIE', 'Lomé',     true, NOW(), 'SYSTEM', true, false),
  ('Direction Générale de la Police Nationale', 'POLICE','Lomé',     true, NOW(), 'SYSTEM', true, false),
  ('Brigade Territoriale de Kpalimé',      'GENDARMERIE', 'Kpalimé',  true, NOW(), 'SYSTEM', true, false),
  ('Commissariat de Kara',                 'POLICE',      'Kara',     true, NOW(), 'SYSTEM', true, false),
  ('Brigade de Dapaong',                   'GENDARMERIE', 'Dapaong',  true, NOW(), 'SYSTEM', true, false),
  ('Brigade Territoriale de Sokodé',       'GENDARMERIE', 'Sokodé',   true, NOW(), 'SYSTEM', true, false),
  ('Commissariat de Tsévié',               'POLICE',      'Tsévié',   true, NOW(), 'SYSTEM', true, false)
ON CONFLICT DO NOTHING;
