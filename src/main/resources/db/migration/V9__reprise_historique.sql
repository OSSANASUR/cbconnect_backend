-- ============================================================
--  V9 — Reprise historique BNCB Togo 2025
--  Uniquement les ALTER TABLE — pas de vues (noms de colonnes
--  à vérifier selon le schéma existant)
-- ============================================================

ALTER TABLE sinistre
ADD COLUMN IF NOT EXISTS reprise_historique BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS assureur_declarant VARCHAR(200),
ADD COLUMN IF NOT EXISTS numero_police_assureur VARCHAR(200);

ALTER TABLE organisme
ADD COLUMN IF NOT EXISTS reprise_historique BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_sinistre_numero_manuel ON sinistre (numero_sinistre_manuel)
WHERE
    numero_sinistre_manuel IS NOT NULL;