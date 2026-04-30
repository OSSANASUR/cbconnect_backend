-- ═══════════════════════════════════════════════════════════════════
-- V27 — Négociation RC par adversaire
-- ═══════════════════════════════════════════════════════════════════
-- Règle métier : la position RC se négocie adversaire par adversaire
-- (la compagnie d'assurance étrangère répond pour chaque adversaire).
-- Un sinistre n'est considéré comme RC « tranchée » que lorsque TOUS
-- ses adversaires ont leur propre position TRANCHEE.
--
-- États par adversaire :
--  EN_ATTENTE      → adversaire créé, aucune proposition BNCB faite
--  EN_NEGOCIATION  → proposition envoyée, en attente réponse compagnie
--  REJETEE         → compagnie a rejeté, contre-proposition BNCB attendue
--  TRANCHEE        → accord final, % verrouillé définitivement
-- ═══════════════════════════════════════════════════════════════════

-- 1) Migration des valeurs existantes au niveau sinistre vers le nouveau vocabulaire
--    (on garde positionRc sur sinistre comme agrégat synchrone)
UPDATE sinistre SET position_rc = 'TRANCHEE' WHERE position_rc = 'ACCEPTE';
UPDATE sinistre SET position_rc = 'REJETEE'  WHERE position_rc = 'REJETE';

-- 2) Colonnes RC portées par chaque victime (utiles uniquement quand est_adversaire = true)
ALTER TABLE victime ADD COLUMN IF NOT EXISTS position_rc              VARCHAR(32) NOT NULL DEFAULT 'EN_ATTENTE';
ALTER TABLE victime ADD COLUMN IF NOT EXISTS pourcentage_rc_propose   INTEGER;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS motif_rejet_rc           TEXT;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS nombre_tours_rc          INTEGER NOT NULL DEFAULT 0;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS pourcentage_rc_final     INTEGER;
ALTER TABLE victime ADD COLUMN IF NOT EXISTS date_derniere_action_rc  TIMESTAMP;

-- 3) Bornes métier
ALTER TABLE victime DROP CONSTRAINT IF EXISTS chk_victime_pourcentage_propose_range;
ALTER TABLE victime ADD CONSTRAINT chk_victime_pourcentage_propose_range
  CHECK (pourcentage_rc_propose IS NULL OR (pourcentage_rc_propose BETWEEN 0 AND 100));

ALTER TABLE victime DROP CONSTRAINT IF EXISTS chk_victime_pourcentage_final_range;
ALTER TABLE victime ADD CONSTRAINT chk_victime_pourcentage_final_range
  CHECK (pourcentage_rc_final IS NULL OR (pourcentage_rc_final BETWEEN 0 AND 100));

-- 4) Index sur date_derniere_action_rc pour les requêtes de relance
CREATE INDEX IF NOT EXISTS idx_victime_date_derniere_action_rc
  ON victime (date_derniere_action_rc)
  WHERE date_derniere_action_rc IS NOT NULL;

-- 5) Backfill : pour chaque adversaire sur un sinistre déjà TRANCHEE avant la migration,
--    on propage l'état au niveau adversaire. Pourcentage historique inconnu → 100 par défaut.
UPDATE victime v
   SET position_rc             = 'TRANCHEE',
       pourcentage_rc_final    = 100,
       date_derniere_action_rc = NOW()
  FROM sinistre s
 WHERE v.sinistre_id = s.historique_id
   AND v.est_adversaire = true
   AND s.position_rc = 'TRANCHEE'
   AND (v.position_rc IS NULL OR v.position_rc = 'EN_ATTENTE');

-- 6) Pour les sinistres REJETEE au niveau dossier, chaque adversaire est considéré REJETEE
UPDATE victime v
   SET position_rc             = 'REJETEE',
       date_derniere_action_rc = NOW()
  FROM sinistre s
 WHERE v.sinistre_id = s.historique_id
   AND v.est_adversaire = true
   AND s.position_rc = 'REJETEE'
   AND (v.position_rc IS NULL OR v.position_rc = 'EN_ATTENTE');
