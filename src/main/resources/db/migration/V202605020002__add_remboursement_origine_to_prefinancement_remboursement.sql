-- Ajout du lien d'origine pour les contre-passages : passage du pattern (b)
-- (active_data=FALSE) au pattern (c) (ligne négative rattachée à l'origine).
ALTER TABLE prefinancement_remboursement
    ADD COLUMN IF NOT EXISTS remboursement_origine_id BIGINT
        REFERENCES prefinancement_remboursement(historique_id);

COMMENT ON COLUMN prefinancement_remboursement.remboursement_origine_id IS
  'Pour les contre-passages : pointe vers la ligne d''origine positive';
