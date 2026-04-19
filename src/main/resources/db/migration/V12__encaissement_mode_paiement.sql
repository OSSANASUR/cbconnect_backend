-- ============================================================
--  V12 — Ajout mode_paiement sur encaissement
--  Valeurs : BANQUE | VIREMENT | ESPECES
-- ============================================================
ALTER TABLE encaissement
    ADD COLUMN IF NOT EXISTS mode_paiement VARCHAR(20);

COMMENT ON COLUMN encaissement.mode_paiement IS 'Mode de paiement : BANQUE, VIREMENT, ESPECES';
