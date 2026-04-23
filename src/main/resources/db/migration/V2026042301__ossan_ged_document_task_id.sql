-- OssanGED : support des documents en attente d'indexation OCR (async)
-- ossan_ged_document_id peut être null le temps que la tâche OssanGED se termine
ALTER TABLE ossan_ged_document ALTER COLUMN ossan_ged_document_id DROP NOT NULL;
ALTER TABLE ossan_ged_document ADD COLUMN IF NOT EXISTS ged_task_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_ossan_ged_document_task_id ON ossan_ged_document(ged_task_id) WHERE ged_task_id IS NOT NULL;
