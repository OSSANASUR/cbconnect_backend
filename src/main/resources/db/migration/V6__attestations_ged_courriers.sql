-- CBConnect V6 – Attestations, GED, Courriers, Barèmes CIMA

-- ── Lot Approvisionnement ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS lot_approvisionnement (
    historique_id  SERIAL PRIMARY KEY,
    lot_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    reference_lot  VARCHAR(50) UNIQUE NOT NULL,
    nom_fournisseur VARCHAR(200) NOT NULL,
    numero_bon_commande VARCHAR(50),
    quantite       INTEGER NOT NULL,
    numero_debut_serie VARCHAR(20) NOT NULL,
    numero_fin_serie   VARCHAR(20) NOT NULL,
    prix_unitaire_achat DECIMAL(15,2) NOT NULL,
    date_commande  DATE NOT NULL,
    date_livraison_fournisseur DATE,
    statut_lot     VARCHAR(20) NOT NULL DEFAULT 'LIVRE',
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'LOT_APPROVISIONNEMENT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_lot_tracking_actif ON lot_approvisionnement (lot_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Commande Attestation ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS commande_attestation (
    historique_id  SERIAL PRIMARY KEY,
    commande_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_commande VARCHAR(30) UNIQUE NOT NULL,
    statut         VARCHAR(30) NOT NULL DEFAULT 'PROFORMA_EMISE',
    quantite       INTEGER NOT NULL,
    prix_unitaire_vente   DECIMAL(15,2) NOT NULL DEFAULT 1075,
    taux_contribution_fonds DECIMAL(15,2) NOT NULL DEFAULT 100,
    montant_attestation    DECIMAL(15,2) NOT NULL,
    montant_contribution_fonds DECIMAL(15,2) NOT NULL,
    montant_total  DECIMAL(15,2) NOT NULL,
    montant_en_lettres TEXT,
    date_commande  DATE NOT NULL DEFAULT CURRENT_DATE,
    date_livraison_effective DATE,
    nom_beneficiaire_cheque VARCHAR(200),
    organisme_id   INTEGER NOT NULL REFERENCES organisme(historique_id),
    lot_id         INTEGER REFERENCES lot_approvisionnement(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'COMMANDE_ATTESTATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_commande_att_tracking_actif ON commande_attestation (commande_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Facture Attestation ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS facture_attestation (
    historique_id  SERIAL PRIMARY KEY,
    facture_attestation_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_facture  VARCHAR(30) UNIQUE NOT NULL,
    type_facture    VARCHAR(15) NOT NULL,
    date_facture    DATE NOT NULL,
    montant_attestation DECIMAL(15,2) NOT NULL,
    montant_contribution_fonds DECIMAL(15,2) NOT NULL,
    montant_total   DECIMAL(15,2) NOT NULL,
    montant_en_lettres TEXT,
    instruction_cheque TEXT,
    date_echeance   DATE,
    paperless_document_id INTEGER,
    annulee         BOOLEAN NOT NULL DEFAULT false,
    motif_annulation TEXT,
    commande_id     INTEGER NOT NULL REFERENCES commande_attestation(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'FACTURE_ATTESTATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_facture_att_tracking_actif ON facture_attestation (facture_attestation_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Chèque Reçu Attestation ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cheque_recu_attestation (
    historique_id  SERIAL PRIMARY KEY,
    cheque_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_cheque   VARCHAR(50) NOT NULL,
    montant         DECIMAL(15,2) NOT NULL,
    banque_emettrice VARCHAR(150),
    date_emission   DATE NOT NULL,
    date_reception  DATE,
    date_encaissement DATE,
    statut          VARCHAR(30) NOT NULL DEFAULT 'RECU',
    motif_annulation TEXT,
    facture_id      INTEGER NOT NULL REFERENCES facture_attestation(historique_id),
    annule_par_id   INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'CHEQUE_RECU_ATTESTATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_cheque_att_tracking_actif ON cheque_recu_attestation (cheque_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Tranche Livraison Attestation ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tranche_livraison_attestation (
    historique_id  SERIAL PRIMARY KEY,
    tranche_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_debut_serie VARCHAR(20) NOT NULL,
    numero_fin_serie   VARCHAR(20) NOT NULL,
    quantite_livree INTEGER NOT NULL,
    date_livraison  DATE NOT NULL,
    commande_id     INTEGER NOT NULL REFERENCES commande_attestation(historique_id),
    lot_id          INTEGER NOT NULL REFERENCES lot_approvisionnement(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'TRANCHE_LIVRAISON_ATTESTATION', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_tranche_tracking_actif ON tranche_livraison_attestation (tranche_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Paperless Dossier ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS paperless_dossier (
    historique_id  SERIAL PRIMARY KEY,
    paperless_dossier_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    paperless_storage_path_id INTEGER,
    paperless_correspondent_id INTEGER,
    chemin_stockage VARCHAR(500) NOT NULL,
    titre           VARCHAR(200) NOT NULL,
    type_dossier    VARCHAR(20) NOT NULL,
    sinistre_id     INTEGER REFERENCES sinistre(historique_id),
    victime_id      INTEGER REFERENCES victime(historique_id),
    parent_dossier_id INTEGER REFERENCES paperless_dossier(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PAPERLESS_DOSSIER', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_paperless_dossier_tracking_actif ON paperless_dossier (paperless_dossier_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Paperless Document ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS paperless_document (
    historique_id  SERIAL PRIMARY KEY,
    paperless_document_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    paperless_document_id INTEGER UNIQUE NOT NULL,
    titre          VARCHAR(200) NOT NULL,
    type_document  VARCHAR(30) NOT NULL,
    date_document  DATE,
    mime_type      VARCHAR(80),
    checksum       VARCHAR(100),
    paperless_tag_id INTEGER,
    paperless_doc_type_id INTEGER,
    dossier_id     INTEGER NOT NULL REFERENCES paperless_dossier(historique_id),
    victime_id     INTEGER REFERENCES victime(historique_id),
    sinistre_id    INTEGER REFERENCES sinistre(historique_id),
    uploade_par_id INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PAPERLESS_DOCUMENT', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_paperless_doc_tracking_actif ON paperless_document (paperless_document_tracking_id) WHERE active_data = true AND deleted_data = false;

-- ── Courrier ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS courrier (
    historique_id  SERIAL PRIMARY KEY,
    courrier_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    reference_courrier VARCHAR(50) UNIQUE NOT NULL,
    type_courrier  VARCHAR(10) NOT NULL CHECK (type_courrier IN ('ENTRANT','SORTANT')),
    nature         VARCHAR(40) NOT NULL,
    expediteur     VARCHAR(200) NOT NULL,
    destinataire   VARCHAR(200) NOT NULL,
    objet          TEXT NOT NULL,
    date_courrier  DATE NOT NULL,
    date_reception DATE,
    canal          VARCHAR(15) NOT NULL DEFAULT 'MAIL',
    reference_bordereau VARCHAR(50),
    traite         BOOLEAN NOT NULL DEFAULT false,
    date_traitement TIMESTAMPTZ,
    paperless_document_id INTEGER,
    sinistre_id    INTEGER REFERENCES sinistre(historique_id),
    traite_par_id  INTEGER REFERENCES utilisateur(historique_id),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'COURRIER', excel BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_courrier_tracking_actif ON courrier (courrier_tracking_id) WHERE active_data = true AND deleted_data = false;
CREATE UNIQUE INDEX uk_courrier_reference_actif ON courrier (reference_courrier) WHERE active_data = true AND deleted_data = false;

-- ── Barèmes CIMA (tables de référence immuables) ───────────────────────────────
CREATE TABLE IF NOT EXISTS bareme_capitalisation (
    id             SERIAL PRIMARY KEY,
    type_bareme    VARCHAR(5) NOT NULL CHECK (type_bareme IN ('M100','F100','M25','F25')),
    age            INTEGER NOT NULL,
    prix_franc_rente DECIMAL(10,4) NOT NULL,
    taux_capitalisation DECIMAL(5,2) NOT NULL,
    table_mortalite VARCHAR(50) NOT NULL,
    age_limite_paiement INTEGER NOT NULL,
    actif          BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (type_bareme, age)
);
COMMENT ON TABLE bareme_capitalisation IS 'Barème de capitalisation CIMA. M100=actif homme, F100=actif femme, M25/F25=pour calcul capital rente mineurs.';

CREATE TABLE IF NOT EXISTS bareme_valeur_point_ip (
    id             SERIAL PRIMARY KEY,
    age_min        INTEGER NOT NULL,
    age_max        INTEGER,
    ipp_min        DECIMAL(5,2) NOT NULL,
    ipp_max        DECIMAL(5,2) NOT NULL,
    valeur_point   INTEGER NOT NULL,
    actif          BOOLEAN NOT NULL DEFAULT true
);
COMMENT ON TABLE bareme_valeur_point_ip IS 'Valeur du point d IP par tranche age/taux. En % du SMIG annuel.';

CREATE TABLE IF NOT EXISTS bareme_cle_repartition_265 (
    id             SERIAL PRIMARY KEY,
    code_situation VARCHAR(50) UNIQUE NOT NULL,
    libelle_situation VARCHAR(200) NOT NULL,
    condition_conjoint BOOLEAN,
    condition_enfant   BOOLEAN,
    nombre_max_enfants INTEGER,
    cle_ascendants     DECIMAL(5,2) NOT NULL DEFAULT 0,
    cle_conjoints      DECIMAL(5,2) NOT NULL DEFAULT 0,
    cle_enfants        DECIMAL(5,2) NOT NULL DEFAULT 0,
    cle_orphelins_doubles DECIMAL(5,2) NOT NULL DEFAULT 0,
    actif              BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS bareme_prejudice_moral_266 (
    id             SERIAL PRIMARY KEY,
    lien_parente   VARCHAR(30) UNIQUE NOT NULL,
    cle            DECIMAL(6,2) NOT NULL,
    plafond_categorie VARCHAR(50),
    actif          BOOLEAN NOT NULL DEFAULT true
);
COMMENT ON TABLE bareme_prejudice_moral_266 IS 'Clés préjudice moral art.266 en % du SMIG annuel.';

-- Seed barèmes préjudice moral art.266 (valeurs CIMA)
INSERT INTO bareme_prejudice_moral_266 (lien_parente, cle, actif)
VALUES
  ('CONJOINT',        200, true),
  ('ENFANT_MINEUR',   100, true),
  ('ENFANT_MAJEUR',    50, true),
  ('PERE',             50, true),
  ('MERE',             50, true),
  ('FRERE_SOEUR',      25, true)
ON CONFLICT DO NOTHING;

-- Seed clés de répartition art.265 (cas principaux)
INSERT INTO bareme_cle_repartition_265 (code_situation, libelle_situation, condition_conjoint, condition_enfant, cle_ascendants, cle_conjoints, cle_enfants)
VALUES
  ('CJ_SEUL',        'Conjoint sans enfant',              true,  false, 0, 100, 0),
  ('CJ_1ENF',        'Conjoint + 1 enfant',               true,  true,  0,  60, 40),
  ('CJ_2ENF',        'Conjoint + 2 enfants',              true,  true,  0,  50, 50),
  ('CJ_3ENF',        'Conjoint + 3 enfants ou plus',      true,  true,  0,  40, 60),
  ('ORPHELINS_SEULS', 'Orphelins sans conjoint',          false, true,  0,   0, 100),
  ('ASCENDANTS_SEULS','Ascendants sans conjoint ni enfant',false, false,100,  0, 0)
ON CONFLICT DO NOTHING;
