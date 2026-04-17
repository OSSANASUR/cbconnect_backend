-- CBConnect – Module Auth & Securite

-- ── Module Entity ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS module_entity (
    historique_id SERIAL PRIMARY KEY,
    module_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    nom_module    VARCHAR(100) NOT NULL,
    description   TEXT,
    actif         BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'MODULE_ENTITY', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_module_tracking_actif ON module_entity (module_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Organisme ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS organisme (
    historique_id  SERIAL PRIMARY KEY,
    organisme_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    type_organisme VARCHAR(25) NOT NULL,
    raison_sociale VARCHAR(200) NOT NULL,
    code           VARCHAR(20) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    responsable    VARCHAR(150),
    code_pays      VARCHAR(5),
    code_pays_bcb  VARCHAR(5),
    pays_id        INTEGER REFERENCES pays(historique_id),
    date_creation  DATE,
    numero_agrement VARCHAR(50),
    api_endpoint_url VARCHAR(255),
    logo           VARCHAR(500),
    active         BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'ORGANISME', excel BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uk_organisme_code UNIQUE (code, active_data, deleted_data)
);
CREATE UNIQUE INDEX uk_organisme_tracking_actif ON organisme (organisme_tracking_id) WHERE active_data = true AND deleted_data = false;
CREATE UNIQUE INDEX uk_organisme_code_actif ON organisme (code) WHERE active_data = true AND deleted_data = false;
CREATE UNIQUE INDEX uk_organisme_email_actif ON organisme (email) WHERE active_data = true AND deleted_data = false;
CREATE TABLE IF NOT EXISTS organisme_contacts (organisme_id INTEGER REFERENCES organisme(historique_id), contact VARCHAR(30));

-- ── Habilitation ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS habilitation (
    historique_id  SERIAL PRIMARY KEY,
    habilitation_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    code_habilitation VARCHAR(50) NOT NULL,
    libelle_habilitation VARCHAR(200),
    description    TEXT,
    action         VARCHAR(20),
    type_acces     VARCHAR(20),
    module_entity_id INTEGER REFERENCES module_entity(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'HABILITATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_habilitation_tracking_actif ON habilitation (habilitation_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Profil ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS profil (
    historique_id SERIAL PRIMARY KEY,
    profil_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    profil_nom     VARCHAR(100) NOT NULL,
    commentaire    TEXT,
    organisme_id   INTEGER REFERENCES organisme(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PROFIL', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_profil_tracking_actif ON profil (profil_tracking_id) WHERE active_data = true AND deleted_data = false;
CREATE TABLE IF NOT EXISTS profil_habilitations (profil_id INTEGER REFERENCES profil(historique_id), habilitation_id INTEGER REFERENCES habilitation(historique_id));

-- ── Utilisateur ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS utilisateur (
    historique_id  SERIAL PRIMARY KEY,
    utilisateur_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    nom            VARCHAR(100) NOT NULL,
    prenoms        VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    username       VARCHAR(80),
    telephone      VARCHAR(30),
    verification_code UUID,
    verification_code_generated_at TIMESTAMPTZ,
    is_active      BOOLEAN NOT NULL DEFAULT true,
    must_change_password BOOLEAN NOT NULL DEFAULT true,
    can_connect_to_multiple_devices BOOLEAN NOT NULL DEFAULT false,
    account_setup_token VARCHAR(255),
    account_setup_token_expires_at TIMESTAMPTZ,
    reset_token    VARCHAR(255),
    reset_token_expires_at TIMESTAMPTZ,
    profil_id      INTEGER REFERENCES profil(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'UTILISATEUR', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_utilisateur_tracking_actif ON utilisateur (utilisateur_tracking_id) WHERE active_data = true AND deleted_data = false;
CREATE UNIQUE INDEX uk_utilisateur_email_actif ON utilisateur (email) WHERE active_data = true AND deleted_data = false;

-- ── Passwords ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS passwords (
    historique_id SERIAL PRIMARY KEY,
    passwords_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    password      VARCHAR(500) NOT NULL,
    is_temporary  BOOLEAN NOT NULL DEFAULT false,
    utilisateur_id INTEGER NOT NULL REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'UTILISATEUR', excel BOOLEAN NOT NULL DEFAULT false
);

-- ── Token ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS token (
    historique_id SERIAL PRIMARY KEY,
    token_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    access_token  VARCHAR(2000),
    refresh_token VARCHAR(2000),
    mobile_token  BOOLEAN NOT NULL DEFAULT false,
    is_valid      BOOLEAN NOT NULL DEFAULT true,
    t_expire_at   TIMESTAMPTZ,
    user_id       INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'UTILISATEUR', excel BOOLEAN NOT NULL DEFAULT false
);

-- ── OTP ───────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS otp (
    historique_id SERIAL PRIMARY KEY,
    otp_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    code          VARCHAR(10) NOT NULL,
    expires_at    TIMESTAMPTZ,
    used          BOOLEAN NOT NULL DEFAULT false,
    purpose       VARCHAR(30),
    utilisateur_id INTEGER NOT NULL REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'UTILISATEUR', excel BOOLEAN NOT NULL DEFAULT false
);

-- ── Parametre ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS parametre (
    historique_id SERIAL PRIMARY KEY,
    parametre_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    type_parametre VARCHAR(20) NOT NULL,
    cle            VARCHAR(100) NOT NULL,
    valeur         TEXT NOT NULL,
    description    TEXT,
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PARAMETRE', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_parametre_cle_actif ON parametre (cle) WHERE active_data = true AND deleted_data = false;

-- Seed parametres systeme CIMA / CEDEAO
INSERT INTO parametre (parametre_tracking_id, type_parametre, cle, valeur, description, created_at, created_by, active_data, deleted_data)
VALUES
  (gen_random_uuid(), 'DELAI',   'DELAI_OFFRE_BLESSE_MOIS',          '12',   'Art. 231 Code CIMA - Delai max offre blesse',       NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'DELAI',   'DELAI_OFFRE_DECES_MOIS',           '8',    'Art. 231 Code CIMA - Delai max offre deces',         NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'TAUX',    'TAUX_PENALITE_RETARD_PCT',         '5',    'Code CIMA - 5% par mois de retard',                 NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'DELAI',   'DELAI_PAIEMENT_APRES_ACCORD_JOURS','30',   'Code CIMA - Paiement dans les 30j apres accord',    NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'DELAI',   'DELAI_REPONSE_VICTIME_JOURS',      '15',   'Code CIMA - La victime a 15j pour repondre',        NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'TAUX',    'TAUX_FRAIS_GESTION_TOGO_PCT',      '5',    'Sinistres SURVENU_TOGO - 5% sur encaissement',      NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'DELAI',   'DELAI_ALERTE_EXPERTISE_JOURS',     '20',   'Alerte rapport expertise non recu apres 20 jours',  NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MONTANT', 'PRIX_UNITAIRE_ATTESTATION_FCFA',   '1075', 'Prix de vente unitaire attestation Carte Brune',    NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MONTANT', 'CONTRIBUTION_FONDS_ATTESTATION_FCFA','100','Contribution fonds de compensation par attestation',NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'DELAI',   'DELAI_ALERTE_PV_JOURS',            '30',   'Alerte sinistre sans PV apres 30 jours',            NOW(), 'SYSTEM', true, false)
ON CONFLICT DO NOTHING;
