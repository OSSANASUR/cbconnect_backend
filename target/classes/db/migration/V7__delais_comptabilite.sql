-- CBConnect V7 – Délais CEDEAO, Comptabilité

-- ── Paramètres Délai ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS parametre_delai (
    id             SERIAL PRIMARY KEY,
    code_delai     VARCHAR(50) UNIQUE NOT NULL,
    libelle        VARCHAR(200) NOT NULL,
    type_delai     VARCHAR(20) NOT NULL,
    categorie      VARCHAR(30) NOT NULL,
    type_sinistre  VARCHAR(25) NOT NULL,
    valeur         DECIMAL(10,2) NOT NULL,
    unite          VARCHAR(25) NOT NULL,
    reference_juridique VARCHAR(100),
    taux_penalite_pct DECIMAL(5,2),
    seuil_alerte1_pct DECIMAL(5,2) DEFAULT 70,
    seuil_alerte2_pct DECIMAL(5,2) DEFAULT 90,
    actif          BOOLEAN NOT NULL DEFAULT true
);

-- ── Notification Délai ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notification_delai (
    id             SERIAL PRIMARY KEY,
    parametre_delai_id INTEGER NOT NULL REFERENCES parametre_delai(id),
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    victime_id     INTEGER REFERENCES victime(historique_id),
    responsable_id INTEGER REFERENCES utilisateur(historique_id),
    date_debut     DATE NOT NULL,
    date_echeance  DATE NOT NULL,
    statut         VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
    niveau_alerte  VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    date_resolution TIMESTAMPTZ,
    motif_resolution TEXT,
    nombre_alertes INTEGER NOT NULL DEFAULT 0,
    derniere_alerte_envoyee TIMESTAMPTZ
);
CREATE INDEX idx_notif_delai_actif ON notification_delai (statut, date_echeance) WHERE statut NOT IN ('RESOLU','ANNULE');
CREATE INDEX idx_notif_delai_sinistre ON notification_delai (sinistre_id);

-- ── Pénalités calculées ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS penalite_calculee (
    id             SERIAL PRIMARY KEY,
    sinistre_id    INTEGER NOT NULL REFERENCES sinistre(historique_id),
    victime_id     INTEGER REFERENCES victime(historique_id),
    parametre_delai_id INTEGER NOT NULL REFERENCES parametre_delai(id),
    date_calcul    DATE NOT NULL,
    montant_base   DECIMAL(15,2) NOT NULL,
    taux_pct_mois  DECIMAL(5,2) NOT NULL,
    nombre_mois_retard INTEGER NOT NULL,
    montant_penalite DECIMAL(15,2) NOT NULL,
    statut         VARCHAR(15) NOT NULL DEFAULT 'CALCULEE',
    motif_annulation TEXT
);

-- ── Plan Comptable (SYSCOHADA adapté BNCB) ────────────────────────────────────
CREATE TABLE IF NOT EXISTS plan_comptable (
    id             SERIAL PRIMARY KEY,
    numero_compte  VARCHAR(10) UNIQUE NOT NULL,
    libelle_compte VARCHAR(200) NOT NULL,
    type_compte    VARCHAR(10) NOT NULL,
    compte_parent_numero VARCHAR(10),
    actif          BOOLEAN NOT NULL DEFAULT true
);

-- ── Journal Comptable ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS journal_comptable (
    id             SERIAL PRIMARY KEY,
    code_journal   VARCHAR(10) UNIQUE NOT NULL,
    libelle_journal VARCHAR(100) NOT NULL,
    type           VARCHAR(30) NOT NULL,
    actif          BOOLEAN NOT NULL DEFAULT true
);

-- ── Règles d'écriture automatiques ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS regle_ecriture (
    id             SERIAL PRIMARY KEY,
    type_transaction VARCHAR(40) UNIQUE NOT NULL,
    libelle        VARCHAR(200) NOT NULL,
    description    TEXT,
    journal_id     INTEGER NOT NULL REFERENCES journal_comptable(id),
    actif          BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS ligne_regle_ecriture (
    id             SERIAL PRIMARY KEY,
    regle_id       INTEGER NOT NULL REFERENCES regle_ecriture(id),
    compte_id      INTEGER NOT NULL REFERENCES plan_comptable(id),
    sens_ligne     VARCHAR(6) NOT NULL CHECK (sens_ligne IN ('DEBIT','CREDIT')),
    expression_montant VARCHAR(50) NOT NULL,
    ordre          INTEGER,
    libelle_ligne  VARCHAR(200)
);

-- ── Écriture Comptable ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ecriture_comptable (
    id             SERIAL PRIMARY KEY,
    ecriture_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero_ecriture VARCHAR(20) UNIQUE NOT NULL,
    type_transaction VARCHAR(40) NOT NULL,
    date_ecriture  DATE NOT NULL,
    libelle        VARCHAR(300) NOT NULL,
    montant_total  DECIMAL(15,2) NOT NULL,
    statut         VARCHAR(15) NOT NULL DEFAULT 'BROUILLON',
    date_validation TIMESTAMPTZ,
    reference_externe_id VARCHAR(100),
    reference_externe_type VARCHAR(50),
    journal_id     INTEGER REFERENCES journal_comptable(id),
    sinistre_id    INTEGER REFERENCES sinistre(historique_id),
    saisi_par_id   INTEGER REFERENCES utilisateur(historique_id),
    valide_par_id  INTEGER REFERENCES utilisateur(historique_id)
);
CREATE INDEX idx_ecriture_date ON ecriture_comptable (date_ecriture DESC);
CREATE INDEX idx_ecriture_sinistre ON ecriture_comptable (sinistre_id);

CREATE TABLE IF NOT EXISTS ligne_ecriture (
    id             SERIAL PRIMARY KEY,
    ecriture_id    INTEGER NOT NULL REFERENCES ecriture_comptable(id),
    compte_id      INTEGER NOT NULL REFERENCES plan_comptable(id),
    sens           VARCHAR(6) NOT NULL CHECK (sens IN ('DEBIT','CREDIT')),
    montant        DECIMAL(15,2) NOT NULL,
    libelle_ligne  VARCHAR(200),
    ordre          INTEGER
);

-- ── SEED : Plan comptable SYSCOHADA adapté BNCB ───────────────────────────────
INSERT INTO plan_comptable (numero_compte, libelle_compte, type_compte, actif) VALUES
  ('4111','Compagnies membres - Attestations','ACTIF',true),
  ('4112','Bureaux homologues - Sinistres','ACTIF',true),
  ('5111','Banque principale BNCB','ACTIF',true),
  ('5112','Banque secondaire','ACTIF',true),
  ('4411','TVA collectée','PASSIF',true),
  ('7011','Vente attestations Carte Brune','PRODUIT',true),
  ('7012','Contribution fonds de compensation','PRODUIT',true),
  ('7013','Produits frais de gestion (5%)','PRODUIT',true),
  ('7410','Remboursements homologues - Sinistres ET','PRODUIT',true),
  ('6011','Achats attestations fournisseur','CHARGE',true),
  ('6521','Indemnisations victimes - Sinistres ET','CHARGE',true),
  ('6522','Indemnisations victimes - Sinistres TE','CHARGE',true),
  ('6523','Préfinancement sinistres','CHARGE',true),
  ('6524','Pénalités de retard CIMA','CHARGE',true),
  ('6611','Honoraires experts médicaux','CHARGE',true),
  ('6612','Honoraires experts automobiles','CHARGE',true)
ON CONFLICT DO NOTHING;

-- ── SEED : Journaux comptables ────────────────────────────────────────────────
INSERT INTO journal_comptable (code_journal, libelle_journal, type, actif) VALUES
  ('BQ', 'Journal de Banque', 'BANQUE', true),
  ('VT', 'Journal des Ventes - Attestations', 'VENTES', true),
  ('HA', 'Journal des Achats', 'ACHATS', true),
  ('OD', 'Opérations Diverses', 'OPERATIONS_DIVERSES', true),
  ('SI', 'Journal Sinistres', 'SINISTRES', true)
ON CONFLICT DO NOTHING;

-- ── SEED : Délais CEDEAO principaux ──────────────────────────────────────────
INSERT INTO parametre_delai (code_delai, libelle, type_delai, categorie, type_sinistre, valeur, unite, reference_juridique, taux_penalite_pct, seuil_alerte1_pct, seuil_alerte2_pct)
VALUES
  -- SURVENU_TOGO (vehicule etranger accidente au Togo)
  ('ET_OFFRE_BLESSE_MOIS',    'Délai offre d''indemnisation - blessé',       'DELAI_MAX', 'OFFRE',         'SURVENU_TOGO',     12, 'MOIS',               'Art. 231 Code CIMA',    5, 70, 90),
  ('ET_OFFRE_DECES_MOIS',     'Délai offre d''indemnisation - décès',        'DELAI_MAX', 'OFFRE',         'SURVENU_TOGO',      8, 'MOIS',               'Art. 231 Code CIMA',    5, 70, 90),
  ('ET_PAIEMENT_APRES_ACCORD','Délai paiement après accord victime',         'DELAI_MAX', 'PAIEMENT',      'SURVENU_TOGO',     30, 'JOURS_CALENDAIRES',  'Art. 231 Code CIMA',    5, 70, 90),
  ('ET_REPONSE_VICTIME',      'Délai réponse de la victime à l''offre',      'DELAI_MAX', 'OFFRE',         'SURVENU_TOGO',     15, 'JOURS_CALENDAIRES',  'Art. 231 Code CIMA',    0, 70, 90),
  ('ET_EXPERTISE_RAPPORT',    'Délai rapport expertise médicale',            'DELAI_MAX', 'EXPERTISE',     'SURVENU_TOGO',     20, 'JOURS_CALENDAIRES',  'Convention CEDEAO',     0, 50, 80),
  ('ET_GARANTIE_MOIS',        'Délai confirmation garantie par l''homologue','DELAI_MAX', 'GARANTIE',      'SURVENU_TOGO',      2, 'MOIS',               'Convention CEDEAO',     0, 70, 90),
  ('ET_PRESCRIPTION_ANS',     'Délai de prescription',                       'PRESCRIPTION','PRESCRIPTION','SURVENU_TOGO',      3, 'ANS',                'Art. CIMA - Prescription',0,50,80),
  -- SURVENU_ETRANGER (vehicule togolais accidente a l''etranger)
  ('TE_TRANSMISSION_HOMOLOGUE','Délai transmission dossier au homologue',    'DELAI_MAX', 'GARANTIE',      'SURVENU_ETRANGER',  1, 'MOIS',               'Convention Harmonisée', 0, 70, 90),
  ('TE_REPONSE_HOMOLOGUE',     'Délai réponse du bureau homologue',          'DELAI_MAX', 'RESPONSABILITE','SURVENU_ETRANGER',  2, 'MOIS',               'Convention Harmonisée', 0, 70, 90),
  ('TE_PRESCRIPTION_ANS',      'Délai de prescription TE',                   'PRESCRIPTION','PRESCRIPTION','SURVENU_ETRANGER',  3, 'ANS',                'Convention Harmonisée', 0, 50, 80),
  -- Délais internes BNCB
  ('INT_ALERTE_PV_JOURS',      'Alerte sinistre sans PV après N jours',       'DELAI_MAX', 'PV',            'SURVENU_TOGO',     30, 'JOURS_CALENDAIRES',  'Procédure interne BNCB',0, 50, 80),
  ('INT_ALERTE_EXPERTISE_JOURS','Alerte rapport expertise non reçu',          'DELAI_MAX', 'EXPERTISE',     'SURVENU_TOGO',     20, 'JOURS_CALENDAIRES',  'Procédure interne BNCB',0, 50, 80)
ON CONFLICT DO NOTHING;
