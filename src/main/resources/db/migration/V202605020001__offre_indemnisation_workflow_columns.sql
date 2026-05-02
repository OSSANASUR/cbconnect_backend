-- V202605020001 — Add workflow de transmission columns to offre_indemnisation
ALTER TABLE offre_indemnisation
    ADD COLUMN IF NOT EXISTS date_envoi_homologue            DATE,
    ADD COLUMN IF NOT EXISTS date_reponse_homologue          DATE,
    ADD COLUMN IF NOT EXISTS montant_contre_offre            DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS description_contre_offre        TEXT,
    ADD COLUMN IF NOT EXISTS ossan_ged_document_id_contre_offre VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_envoi_victime              DATE,
    ADD COLUMN IF NOT EXISTS date_accord_victime             DATE,
    ADD COLUMN IF NOT EXISTS observations_accord             TEXT,
    ADD COLUMN IF NOT EXISTS ossan_ged_document_id_accord    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS date_rejet_victime              DATE;
