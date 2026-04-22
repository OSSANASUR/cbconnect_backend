-- CBConnect V22 — Wizard de déclaration étendu (7 étapes)
-- Ajoute les colonnes nécessaires pour la fiche de déclaration complète.
-- Entités touchées : assure · sinistre · victime  (+ nouvelle table sinistre_assureur_secondaire)

-- ─────────────────────────────────────────────────────────────
-- 1. ASSURE — champs enrichis (profession, véhicule détaillé)
-- ─────────────────────────────────────────────────────────────
ALTER TABLE assure ADD COLUMN IF NOT EXISTS profession           VARCHAR(150);
ALTER TABLE assure ADD COLUMN IF NOT EXISTS prochaine_vt         DATE;
ALTER TABLE assure ADD COLUMN IF NOT EXISTS capacite_vehicule    INTEGER;
ALTER TABLE assure ADD COLUMN IF NOT EXISTS nb_personnes_a_bord  INTEGER;
ALTER TABLE assure ADD COLUMN IF NOT EXISTS a_remorque           BOOLEAN NOT NULL DEFAULT false;

-- ─────────────────────────────────────────────────────────────
-- 2. SINISTRE — accident détaillé + conducteur flat + déclarant flat
-- ─────────────────────────────────────────────────────────────
-- Accident
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS heure_accident        TIME;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS ville                 VARCHAR(150);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS commune               VARCHAR(150);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS provenance            VARCHAR(200);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS destination           VARCHAR(200);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS circonstances         TEXT;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS pv_etabli             BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS entite_constat_id     INTEGER REFERENCES entite_constat(historique_id);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS date_effet            DATE;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS date_echeance         DATE;

-- Conducteur (flat, pas d'entité dédiée pour l'instant)
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_est_assure  BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_nom         VARCHAR(200);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_prenom      VARCHAR(200);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_date_naissance  DATE;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_numero_permis   VARCHAR(50);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_categories_permis VARCHAR(150);  -- CSV ex "B,C"
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_date_delivrance  DATE;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS conducteur_lieu_delivrance  VARCHAR(150);

-- Déclarant (flat)
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS declarant_nom         VARCHAR(200);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS declarant_prenom      VARCHAR(200);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS declarant_telephone   VARCHAR(30);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS declarant_qualite     VARCHAR(30);

-- ─────────────────────────────────────────────────────────────
-- 3. Assureurs secondaires (table de jointure Sinistre × Organisme)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sinistre_assureur_secondaire (
    sinistre_id   INTEGER NOT NULL REFERENCES sinistre(historique_id) ON DELETE CASCADE,
    organisme_id  INTEGER NOT NULL REFERENCES organisme(historique_id),
    PRIMARY KEY (sinistre_id, organisme_id)
);
CREATE INDEX IF NOT EXISTS idx_sin_assur_sec_org ON sinistre_assureur_secondaire(organisme_id);

-- ─────────────────────────────────────────────────────────────
-- 4. VICTIME — flag adversaire + extras pour conducteur/véhicule/dommages adverses
-- ─────────────────────────────────────────────────────────────
-- Commun
ALTER TABLE victime ADD COLUMN IF NOT EXISTS est_adversaire        BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS profession            VARCHAR(150);
ALTER TABLE victime ADD COLUMN IF NOT EXISTS type_dommage          VARCHAR(10);

-- Adversaire : conducteur
ALTER TABLE victime ADD COLUMN IF NOT EXISTS telephone             VARCHAR(30);
ALTER TABLE victime ADD COLUMN IF NOT EXISTS numero_permis         VARCHAR(50);
ALTER TABLE victime ADD COLUMN IF NOT EXISTS categories_permis     VARCHAR(150);
ALTER TABLE victime ADD COLUMN IF NOT EXISTS date_delivrance       DATE;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS lieu_delivrance       VARCHAR(150);

-- Adversaire : véhicule
ALTER TABLE victime ADD COLUMN IF NOT EXISTS marque_vehicule       VARCHAR(100);
ALTER TABLE victime ADD COLUMN IF NOT EXISTS immatriculation       VARCHAR(30);
ALTER TABLE victime ADD COLUMN IF NOT EXISTS prochaine_vt          DATE;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS nb_personnes_a_bord   INTEGER;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS assureur_adverse      VARCHAR(200);

-- Adversaire : dommages
ALTER TABLE victime ADD COLUMN IF NOT EXISTS description_degats    TEXT;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS blesses_legers        INTEGER NOT NULL DEFAULT 0;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS blesses_graves        INTEGER NOT NULL DEFAULT 0;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS deces                 INTEGER NOT NULL DEFAULT 0;

-- Index utile pour filtrer les victimes réelles vs adversaires
CREATE INDEX IF NOT EXISTS idx_victime_adversaire ON victime (sinistre_id, est_adversaire)
    WHERE active_data = true AND deleted_data = false;

COMMENT ON TABLE sinistre_assureur_secondaire IS 'Assureurs secondaires (remorque, co-assurance) liés à un sinistre.';
COMMENT ON COLUMN sinistre.conducteur_est_assure IS 'Si vrai : le conducteur au moment du sinistre est l assuré lui-même.';
COMMENT ON COLUMN sinistre.declarant_qualite IS 'Lien du déclarant avec l assuré : ASSURE|CONDUCTEUR|PROCHE|TEMOIN|ENVOYE|AUTRE';
COMMENT ON COLUMN victime.est_adversaire IS 'Distingue les occupants du véhicule tiers (adversaires) des victimes côté assuré.';
