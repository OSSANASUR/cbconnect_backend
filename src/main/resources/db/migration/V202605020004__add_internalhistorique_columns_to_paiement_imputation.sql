-- Ajoute les colonnes héritées de InternalHistorique manquantes dans
-- paiement_imputation (oubli initial dans V202605020001) :
-- libelle, parent_code_id, excel — toutes les autres entités InternalHistorique
-- les exposent (cf. lot_reglement, dossier_reclamation, etc.).
ALTER TABLE paiement_imputation
    ADD COLUMN IF NOT EXISTS libelle        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS parent_code_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS excel          BOOLEAN NOT NULL DEFAULT FALSE;
