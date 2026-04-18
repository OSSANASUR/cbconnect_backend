-- ============================================================
--  V13 — Ajout reprise_historique sur encaissement
-- ============================================================
ALTER TABLE encaissement
    ADD COLUMN IF NOT EXISTS reprise_historique BOOLEAN NOT NULL DEFAULT FALSE;
