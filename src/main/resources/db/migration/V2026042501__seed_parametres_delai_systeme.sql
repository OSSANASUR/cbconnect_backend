-- V2026042501 — Seed référentiel délais CEDEAO (35 codes) + table parametre_systeme

-- ── Ajout colonne modifiable sur parametre_delai ──────────────────────────────
ALTER TABLE parametre_delai ADD COLUMN IF NOT EXISTS modifiable BOOLEAN NOT NULL DEFAULT true;

-- ── Seed des 35 codes délais validés BNCB ────────────────────────────────────
-- Seuils alertes conformes au document validé : 50 % (ATTENTION) / 75 % (URGENT) / 100 % (CRITIQUE)
INSERT INTO parametre_delai
  (code_delai, libelle, type_delai, categorie, type_sinistre, valeur, unite,
   reference_juridique, taux_penalite_pct, seuil_alerte1_pct, seuil_alerte2_pct, modifiable, actif)
VALUES
  -- ── GARANTIE ────────────────────────────────────────────────────────────────
  ('DLI_GRT_001','Conf. garantie – Bureau Émetteur',
   'DELAI_MAX','GARANTIE','TOUS', 7,'JOURS_OUVRES',
   'Guide Pratique p.1 · Accord Art.2',NULL,50,75,false,true),

  ('DLI_GRT_002','Conf. garantie – Compagnie Membre',
   'DELAI_MAX','GARANTIE','TOUS', 7,'JOURS_OUVRES',
   'Guide Pratique p.2 · Accord Art.2',NULL,50,75,false,true),

  ('DLI_RSP_001','Discussions responsabilité RC',
   'DELAI_MAX','RESPONSABILITE','TOUS', 30,'JOURS_CALENDAIRES',
   'Guide Pratique p.1 · Accord Art.3',NULL,50,75,false,true),

  ('DLI_PV_001','Transmission PV aux assureurs',
   'DELAI_MAX','PV','TOUS', 2,'MOIS',
   'Conv. Harm. Art.31 / Art.81',NULL,50,75,false,true),

  -- ── EXPERTISE ───────────────────────────────────────────────────────────────
  ('DLI_INS_001','Approbation Bureau Émetteur > 3 000 UC',
   'DELAI_MAX','EXPERTISE','TOUS', 15,'JOURS_CALENDAIRES',
   'Guide Pratique p.3 · Accord Art.4',NULL,50,75,false,true),

  ('DLI_EXP_001','Contre-expertise matérielle',
   'DELAI_MAX','EXPERTISE','TOUS', 30,'JOURS_CALENDAIRES',
   'Guide Pratique p.4 · Accord Art.6',NULL,50,75,false,true),

  ('DLI_MED_001','Préavis victime avant examen médical (minimum)',
   'DELAI_MIN','EXPERTISE','TOUS', 15,'JOURS_CALENDAIRES',
   'Guide Pratique p.5 · Accord Art.12 · Conv. Harm. Art.46',NULL,50,75,false,true),

  ('DLI_MED_002','Rapport expertise médicale par l''expert désigné',
   'DELAI_MAX','EXPERTISE','TOUS', 30,'JOURS_CALENDAIRES',
   'Guide Pratique p.5 · Accord Art.12 · Conv. Harm. Art.47',NULL,50,75,false,true),

  ('DLI_MED_003','Envoi rapport médical aux deux parties',
   'DELAI_MAX','EXPERTISE','TOUS', 20,'JOURS_CALENDAIRES',
   'Conv. Harm. Art.47',NULL,50,75,false,true),

  -- ── OFFRE ───────────────────────────────────────────────────────────────────
  ('DLI_OFF_001','Offre d''indemnisation – victime blessée',
   'DELAI_MAX','OFFRE','TOUS', 6,'MOIS',
   'Conv. Harm. Art.32 §1',NULL,50,75,false,true),

  ('DLI_OFF_002','Offre d''indemnisation – décès (dossier complet)',
   'DELAI_MAX','OFFRE','TOUS', 1,'MOIS',
   'Conv. Harm. Art.32 §2',NULL,50,75,false,true),

  ('DLI_OFF_003','Offre d''indemnisation – décès (délai absolu)',
   'DELAI_MAX','OFFRE','TOUS', 6,'MOIS',
   'Conv. Harm. Art.32 §2',NULL,50,75,false,true),

  ('DLI_OFF_004','Offre définitive post-consolidation',
   'DELAI_MAX','OFFRE','TOUS', 2,'MOIS',
   'Conv. Harm. Art.32 §6',NULL,50,75,false,true),

  -- ── PAIEMENT ────────────────────────────────────────────────────────────────
  ('DLI_DEN_001','Dénonciation transaction par la victime',
   'DELAI_MAX','PAIEMENT','TOUS', 15,'JOURS_CALENDAIRES',
   'Guide Pratique p.7 · Accord Art.16 · Conv. Harm. Art.36',NULL,50,75,false,true),

  ('DLI_PAI_001','Paiement indemnités (sinistres ≤ 3 000 UC)',
   'DELAI_MAX','PAIEMENT','TOUS', 30,'JOURS_CALENDAIRES',
   'Guide Pratique p.4 · Accord Art.7 · Conv. Harm. Art.82',NULL,50,75,false,true),

  ('DLI_PAI_002','Accord préalable Bureau Émetteur (> 3 000 UC)',
   'DELAI_MAX','PAIEMENT','TOUS', 3,'MOIS',
   'Guide Pratique p.4 · Accord Art.7 · Conv. Harm. Art.82',NULL,50,75,false,true),

  ('DLI_PAI_003','Paiement après expiration délai de dénonciation',
   'DELAI_MAX','PAIEMENT','TOUS', 15,'JOURS_CALENDAIRES',
   'Guide Pratique p.7 · Accord Art.16 · Conv. Harm. Art.37',NULL,50,75,false,true),

  ('DLI_PAI_004','PÉNALITÉ – Intérêts de retard sur paiement indemnité',
   'PENALITE_AUTO','PAIEMENT','TOUS', 5,'PCT_PAR_MOIS',
   'Conv. Harm. Art.37',5.00,50,75,true,true),

  -- ── RECOURS ─────────────────────────────────────────────────────────────────
  ('DLI_REC_001','Remboursement recours subrogatoire – Bureau Émetteur',
   'DELAI_MAX','RECOURS','TOUS', 30,'JOURS_CALENDAIRES',
   'Guide Pratique p.4 · Accord Art.8 · Conv. Harm. Art.83',NULL,50,75,false,true),

  ('DLI_REC_002','Remboursement recours subrogatoire – Compagnie Membre',
   'DELAI_MAX','RECOURS','TOUS', 30,'JOURS_CALENDAIRES',
   'Guide Pratique p.4',NULL,50,75,false,true),

  ('DLI_REC_003','PÉNALITÉ – Retard remboursement recours subrogatoire',
   'PENALITE_AUTO','RECOURS','TOUS', 3,'PCT_PAR_MOIS',
   'Conv. Harm. Art.83',3.00,50,75,true,true),

  -- ── PRESCRIPTION ────────────────────────────────────────────────────────────
  ('DLI_PSC_001','Prescription RC générale',
   'PRESCRIPTION','PRESCRIPTION','TOUS', 5,'ANS',
   'Conv. Harm. Art.57',NULL,50,75,false,true),

  ('DLI_PSC_002','Prescription étendue (accident corporel grave)',
   'PRESCRIPTION','PRESCRIPTION','TOUS', 10,'ANS',
   'Conv. Harm. Art.57',NULL,50,75,false,true),

  -- ── DÉLAIS INTERNES OPÉRATIONNELS ───────────────────────────────────────────
  ('DLI_INT_001','Déclaration non validée',
   'INTERNE','INTERNE','TOUS', 2,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_002','1ère relance pièces manquantes',
   'INTERNE','INTERNE','TOUS', 15,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_003','2ème relance pièces manquantes',
   'INTERNE','INTERNE','TOUS', 30,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_004','Alerte expertise sans rapport',
   'INTERNE','INTERNE','TOUS', 20,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_005','Courrier entrant non traité',
   'INTERNE','INTERNE','TOUS', 3,'JOURS_OUVRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_006','Dossier MÛR non calculé',
   'INTERNE','INTERNE','TOUS', 5,'JOURS_OUVRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_007','Calcul effectué non transmis',
   'INTERNE','INTERNE','TOUS', 3,'JOURS_OUVRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_008','Accord sans BAP (bon à payer)',
   'INTERNE','INTERNE','TOUS', 5,'JOURS_OUVRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_009','BAP sans chèque reçu',
   'INTERNE','INTERNE','TOUS', 15,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_010','Chèque reçu non encaissé',
   'INTERNE','INTERNE','TOUS', 5,'JOURS_OUVRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_011','Audience judiciaire imminente',
   'INTERNE','INTERNE','TOUS', 5,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,true,true),

  ('DLI_INT_012','Chèque revenu sans provision',
   'INTERNE','INTERNE','TOUS', 1,'JOURS_CALENDAIRES',
   'Opérationnel BNCB',NULL,50,75,false,true)

ON CONFLICT (code_delai) DO UPDATE SET
  libelle             = EXCLUDED.libelle,
  valeur              = EXCLUDED.valeur,
  seuil_alerte1_pct   = EXCLUDED.seuil_alerte1_pct,
  seuil_alerte2_pct   = EXCLUDED.seuil_alerte2_pct,
  taux_penalite_pct   = EXCLUDED.taux_penalite_pct,
  modifiable          = EXCLUDED.modifiable;

-- ── Table parametre_systeme ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS parametre_systeme (
    id              SERIAL PRIMARY KEY,
    cle             VARCHAR(80) UNIQUE NOT NULL,
    libelle         VARCHAR(200) NOT NULL,
    valeur_decimal  DECIMAL(10,4),
    description     TEXT,
    actif           BOOLEAN NOT NULL DEFAULT true
);

INSERT INTO parametre_systeme (cle, libelle, valeur_decimal, description)
VALUES
  ('PCT_FRAIS_GESTION',
   'Frais de gestion (%)',
   5.00,
   'Pourcentage de frais de gestion appliqué sur l''indemnité nette pour les sinistres survenus au Togo.')
ON CONFLICT (cle) DO NOTHING;
