-- CBConnect V5 – Finance, Indemnisation, Réclamations, Attestations

-- ── Dossier Réclamation ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS dossier_reclamation (
    historique_id SERIAL PRIMARY KEY,
    dossier_reclamation_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_dossier  VARCHAR(30) UNIQUE NOT NULL,
    date_ouverture  DATE NOT NULL DEFAULT CURRENT_DATE,
    date_cloture    DATE,
    statut          VARCHAR(20) NOT NULL DEFAULT 'OUVERT',
    montant_total_reclame DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_total_retenu  DECIMAL(15,2) NOT NULL DEFAULT 0,
    notes_redacteur TEXT,
    sinistre_id     INTEGER NOT NULL REFERENCES sinistre(historique_id),
    victime_id      INTEGER NOT NULL REFERENCES victime(historique_id),
    redacteur_id    INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'DOSSIER_RECLAMATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_dossier_rec_tracking_actif ON dossier_reclamation (dossier_reclamation_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Facture Réclamation ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS facture_reclamation (
    historique_id SERIAL PRIMARY KEY,
    facture_reclamation_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_facture_original VARCHAR(80),
    type_depense    VARCHAR(40) NOT NULL,
    nom_prestataire VARCHAR(200) NOT NULL,
    date_facture    DATE NOT NULL,
    montant_reclame DECIMAL(15,2) NOT NULL,
    montant_retenu  DECIMAL(15,2),
    statut_traitement VARCHAR(25) NOT NULL DEFAULT 'EN_ATTENTE',
    motif_rejet     TEXT,
    lien_avec_accident_verifie BOOLEAN NOT NULL DEFAULT false,
    date_traitement DATE,
    paperless_document_id INTEGER,
    dossier_reclamation_id INTEGER NOT NULL REFERENCES dossier_reclamation(historique_id),
    traite_par_id   INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'FACTURE_RECLAMATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_facture_rec_tracking_actif ON facture_reclamation (facture_reclamation_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Offre Indemnisation ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS offre_indemnisation (
    historique_id  SERIAL PRIMARY KEY,
    offre_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    smig_mensuel_retenu DECIMAL(15,2) NOT NULL,
    montant_frais_medicaux     DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_itt                DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_physiologique DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_economique    DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_moral         DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_tierce_personne    DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_pretium_doloris    DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_esthetique    DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_carriere      DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_scolaire      DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_prej_leses         DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_frais_funeraires   DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_brut                 DECIMAL(15,2) NOT NULL,
    taux_partage_rc            DECIMAL(5,2)  NOT NULL DEFAULT 100,
    total_net                  DECIMAL(15,2) NOT NULL,
    frais_gestion              DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_total_offre        DECIMAL(15,2) NOT NULL,
    detail_calcul_json         JSONB,
    date_validation            TIMESTAMPTZ,
    victime_id                 INTEGER NOT NULL REFERENCES victime(historique_id),
    valide_par_id              INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'OFFRE_INDEMNISATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_offre_tracking_actif ON offre_indemnisation (offre_tracking_id) WHERE active_data = true AND deleted_data = false;
COMMENT ON TABLE offre_indemnisation IS 'Archive complete du calcul CIMA art.258-266. Voir CalculCimaServiceImpl pour les formules.';

-- ── Ayant Droit ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ayant_droit (
    historique_id  SERIAL PRIMARY KEY,
    ayant_droit_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    nom            VARCHAR(100) NOT NULL,
    prenoms        VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    sexe           CHAR(1) NOT NULL,
    lien           VARCHAR(20) NOT NULL,
    est_orphelin_double BOOLEAN NOT NULL DEFAULT false,
    poursuite_etudes    BOOLEAN NOT NULL DEFAULT false,
    montant_pe     DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_pm     DECIMAL(15,2) NOT NULL DEFAULT 0,
    montant_total  DECIMAL(15,2) NOT NULL DEFAULT 0,
    victime_id     INTEGER NOT NULL REFERENCES victime(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'AYANT_DROIT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_ayant_droit_tracking_actif ON ayant_droit (ayant_droit_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Encaissement ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS encaissement (
    historique_id  SERIAL PRIMARY KEY,
    encaissement_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_cheque  VARCHAR(50) NOT NULL,
    montant_cheque DECIMAL(15,2) NOT NULL,
    montant_theorique DECIMAL(15,2) NOT NULL,
    produit_frais_gestion DECIMAL(15,2) NOT NULL DEFAULT 0,
    date_emission  DATE NOT NULL,
    date_reception DATE,
    date_encaissement DATE,
    banque_emettrice VARCHAR(150),
    statut_cheque  VARCHAR(30) NOT NULL DEFAULT 'RECU',
    motif_annulation TEXT,
    organisme_emetteur_id INTEGER NOT NULL REFERENCES organisme(historique_id),
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    annule_par_id  INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'ENCAISSEMENT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_encaissement_tracking_actif ON encaissement (encaissement_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Paiement ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS paiement (
    historique_id  SERIAL PRIMARY KEY,
    paiement_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    beneficiaire   VARCHAR(200) NOT NULL,
    beneficiaire_victime_id INTEGER NOT NULL REFERENCES victime(historique_id),
    beneficiaire_organisme_id INTEGER REFERENCES organisme(historique_id),
    numero_cheque_emis VARCHAR(50) NOT NULL,
    banque_cheque  VARCHAR(150) NOT NULL,
    montant        DECIMAL(15,2) NOT NULL,
    date_emission  DATE NOT NULL,
    date_paiement  DATE,
    statut         VARCHAR(20) NOT NULL DEFAULT 'EMIS',
    motif_annulation TEXT,
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    encaissement_id INTEGER REFERENCES encaissement(historique_id),
    annule_par_id  INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PAIEMENT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_paiement_tracking_actif ON paiement (paiement_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Préfinancement ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS prefinancement (
    historique_id  SERIAL PRIMARY KEY,
    prefinancement_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    montant_prefinance DECIMAL(15,2) NOT NULL,
    date_prefinancement DATE NOT NULL,
    date_remboursement DATE,
    est_rembourse  BOOLEAN NOT NULL DEFAULT false,
    montant_rembourse DECIMAL(15,2) NOT NULL DEFAULT 0,
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PREFINANCEMENT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_prefinancement_tracking_actif ON prefinancement (prefinancement_tracking_id) WHERE active_data = true AND deleted_data = false;
