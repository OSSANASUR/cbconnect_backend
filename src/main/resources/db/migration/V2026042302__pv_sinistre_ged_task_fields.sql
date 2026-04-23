-- Champs de suivi asynchrone OssanGED sur pv_sinistre
-- Permettent de tracer la tâche OCR et son statut d'indexation
ALTER TABLE pv_sinistre
    ADD COLUMN IF NOT EXISTS ossan_ged_task_id           VARCHAR(64),
    ADD COLUMN IF NOT EXISTS ossan_ged_indexation_statut VARCHAR(30),
    ADD COLUMN IF NOT EXISTS ossan_ged_indexation_message VARCHAR(1000);
