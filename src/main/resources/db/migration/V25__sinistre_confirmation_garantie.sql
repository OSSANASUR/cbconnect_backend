-- ============================================================
--  V24 — Confirmation de garantie (acquise vs non acquise)
--
--  Après la déclaration, le BNCB doit confirmer avec l'assureur
--  étranger si la garantie Carte Brune est acquise sur le véhicule.
--  Résultat : soit ATTESTATION DE DÉCLARATION (garantie acquise)
--  soit ATTESTATION DE NON GARANTIE.
-- ============================================================

ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS garantie_acquise BOOLEAN;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS reference_garantie VARCHAR(50);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS date_confirmation_garantie DATE;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS observations_garantie TEXT;
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS courrier_non_garantie_ref VARCHAR(120);
ALTER TABLE sinistre ADD COLUMN IF NOT EXISTS courrier_non_garantie_date DATE;
