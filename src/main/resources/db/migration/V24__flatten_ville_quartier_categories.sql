-- ============================================================
--  V24 — Aplatissement des catégories VILLE_XX / QUARTIER_XX / COMMUNE_XX
--
--  Décision produit (2026-04-21) : convention flat pour les listes de lieux.
--  Le front interroge désormais /v1/parametres/categorie/VILLE (idem
--  QUARTIER, COMMUNE) sans préfixe pays. Les entrées déjà créées sous
--  VILLE_TG, QUARTIER_TG, etc. doivent migrer vers la catégorie plate.
--
--  Étapes :
--   1. Si un doublon existe déjà (ex. VILLE.LOME ET VILLE_TG.LOME),
--      on conserve la version flat et on soft-delete la variante namespacée.
--   2. Sinon on renomme VILLE_XX.<code> → VILLE.<code> (pareil pour les deux
--      autres catégories).
-- ============================================================

-- Étape 1 : soft-delete les doublons (flat déjà présent avec le même code)
UPDATE parametre p
SET
    active_data = false,
    deleted_data = true,
    deleted_at = NOW(),
    deleted_by = 'SYSTEM_FLATTEN_V24'
WHERE p.active_data = true
  AND p.deleted_data = false
  AND p.cle ~ '^(VILLE|QUARTIER|COMMUNE)_[A-Z]{2}\.'
  AND EXISTS (
      SELECT 1 FROM parametre p2
      WHERE p2.cle = regexp_replace(p.cle, '^(VILLE|QUARTIER|COMMUNE)_[A-Z]{2}\.', '\1.')
        AND p2.active_data = true
        AND p2.deleted_data = false
  );

-- Étape 2 : renommer les entrées restantes vers la catégorie flat
UPDATE parametre p
SET
    cle = regexp_replace(p.cle, '^(VILLE|QUARTIER|COMMUNE)_[A-Z]{2}\.', '\1.'),
    updated_at = NOW(),
    updated_by = 'SYSTEM_FLATTEN_V24'
WHERE p.active_data = true
  AND p.deleted_data = false
  AND p.cle ~ '^(VILLE|QUARTIER|COMMUNE)_[A-Z]{2}\.';

-- Observabilité : compter les migrations effectuées
DO $$
DECLARE
    renommes INTEGER;
    reste_doublons INTEGER;
BEGIN
    SELECT COUNT(*) INTO renommes
    FROM parametre
    WHERE updated_by = 'SYSTEM_FLATTEN_V24';

    SELECT COUNT(*) INTO reste_doublons
    FROM parametre
    WHERE deleted_by = 'SYSTEM_FLATTEN_V24';

    RAISE NOTICE 'V24 flatten : % entrée(s) renommée(s) vers flat, % doublon(s) soft-supprimé(s)', renommes, reste_doublons;
END $$;
