-- V21 : Enrichissement du PV pour l'enregistrement de bout-en-bout
--
-- 1. detail_provenance : complément libre à la provenance enum (ex: nom du bureau homologue, contact particulier...)
-- 2. document_local_*  : métadonnées du PV scanné stocké localement, en attendant la mise en service d'Ossan GED.
--    Quand la GED sera branchée, un job remplira ossan_ged_document_id et pourra vider les colonnes locales.

ALTER TABLE pv_sinistre ADD COLUMN IF NOT EXISTS detail_provenance    VARCHAR(256);
ALTER TABLE pv_sinistre ADD COLUMN IF NOT EXISTS document_local_path  VARCHAR(512);
ALTER TABLE pv_sinistre ADD COLUMN IF NOT EXISTS document_nom_fichier VARCHAR(255);
ALTER TABLE pv_sinistre ADD COLUMN IF NOT EXISTS document_mime_type   VARCHAR(100);
ALTER TABLE pv_sinistre ADD COLUMN IF NOT EXISTS document_taille      BIGINT;
ALTER TABLE pv_sinistre ADD COLUMN IF NOT EXISTS document_uploaded_at TIMESTAMPTZ;

COMMENT ON COLUMN pv_sinistre.detail_provenance IS
    'Précision libre sur la provenance (nom du bureau homologue, contact, etc.) complémentaire au champ provenance.';
COMMENT ON COLUMN pv_sinistre.document_local_path IS
    'Chemin local du PV scanné tant qu''Ossan GED n''est pas branché. NULL une fois poussé vers la GED.';
COMMENT ON COLUMN pv_sinistre.document_nom_fichier IS
    'Nom d''origine du fichier uploadé (affichage + téléchargement).';

CREATE INDEX IF NOT EXISTS idx_pv_a_document_local
    ON pv_sinistre (document_local_path)
    WHERE document_local_path IS NOT NULL AND active_data = TRUE AND deleted_data = FALSE;
