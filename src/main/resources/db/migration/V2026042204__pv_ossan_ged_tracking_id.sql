-- CBConnect V22 — Ajout du tracking UUID OssanGED sur pv_sinistre
--
-- Permet de naviguer proprement depuis le PV vers l'entité OssanGedDocument
-- en base CBConnect (clé stable UUID, contrairement à ossan_ged_document_id
-- qui est l'ID interne du moteur GED).

ALTER TABLE pv_sinistre
    ADD COLUMN IF NOT EXISTS ossan_ged_document_tracking_id UUID;

CREATE INDEX IF NOT EXISTS idx_pv_ossan_ged_doc_tracking
    ON pv_sinistre (ossan_ged_document_tracking_id)
    WHERE ossan_ged_document_tracking_id IS NOT NULL;
