-- ============================================================
--  V15 — Colonnes audit InternalHistorique manquantes
--        sur les tables créées en V14
-- ============================================================

ALTER TABLE piece_dossier_reclamation
    ADD COLUMN IF NOT EXISTS excel    BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS libelle  VARCHAR(255);

ALTER TABLE type_piece_administrative
    ADD COLUMN IF NOT EXISTS excel    BOOLEAN NOT NULL DEFAULT FALSE;
