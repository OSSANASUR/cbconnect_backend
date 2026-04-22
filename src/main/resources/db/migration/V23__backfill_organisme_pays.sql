-- ============================================================
--  V23 — Backfill code_pays / code_pays_bcb / pays_id des organismes
--
--  Problème : avant le correctif frontend de 2026-04-21, le formulaire
--  d'ajout d'organisme envoyait `paysTrackingId` alors que le DTO
--  `OrganismeRequest` attend `code_pays`, `code_pays_bcb`, `pays_id`.
--  Résultat : tous les organismes créés via le formulaire avaient ces
--  trois colonnes à NULL, ce qui cassait :
--    - le filtre par pays sur la page Organismes
--    - la vue hiérarchique Bureau National → Compagnies
--
--  Stratégie : extraire le code Carte Brune (2 lettres) depuis la
--  colonne `code` en se basant sur la nomenclature observée :
--    BNCB-TG, BCB-BJ, NSIA-TG, SUNU-BF, SAHAM-CI  (séparateur '-')
--    SUNU BJ, SAHAM CI                            (séparateur espace, reprise Excel)
--    BF, SN                                       (code 2 lettres standalone)
--  puis joindre sur pays.code_carte_brune.
--
--  Les organismes dont le code ne suit aucun pattern reconnu restent
--  à NULL — ils devront être corrigés manuellement via l'UI.
-- ============================================================

WITH candidats AS (
    SELECT
        o.historique_id,
        -- Dernier segment de 2 lettres (après '-' ou espace) ou code entier si exactement 2 lettres
        CASE
            WHEN upper(o.code) ~ '[- ][A-Z]{2}$' THEN upper(substring(upper(o.code) from '.*[- ]([A-Z]{2})$'))
            WHEN upper(o.code) ~ '^[A-Z]{2}$'    THEN upper(o.code)
            ELSE NULL
        END AS code_bcb_extrait
    FROM organisme o
    WHERE o.code_pays IS NULL
      AND o.active_data = true
      AND o.deleted_data = false
)
UPDATE organisme o
SET
    code_pays = p.code_iso,
    code_pays_bcb = p.code_carte_brune,
    pays_id = p.historique_id,
    updated_at = NOW(),
    updated_by = COALESCE(o.updated_by, 'SYSTEM_BACKFILL_V23')
FROM candidats c
    JOIN pays p ON p.code_carte_brune = c.code_bcb_extrait
    AND p.active_data = true
    AND p.deleted_data = false
WHERE o.historique_id = c.historique_id
  AND c.code_bcb_extrait IS NOT NULL;

-- Trace applicative : combien d'organismes résistent au backfill ?
DO $$
DECLARE
    orphelins_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO orphelins_count
    FROM organisme
    WHERE code_pays IS NULL
      AND active_data = true
      AND deleted_data = false;
    IF orphelins_count > 0 THEN
        RAISE NOTICE 'V23 backfill : % organisme(s) sans code_pays après tentative — à corriger manuellement', orphelins_count;
    END IF;
END $$;
