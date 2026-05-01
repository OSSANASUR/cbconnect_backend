-- V202604301015__lot_reglement_numero_lot.sql
-- Numéro lisible du lot — format LT{seq:03d}/{yyyy}, séquence annuelle globale.

ALTER TABLE lot_reglement
    ADD COLUMN numero_lot VARCHAR(30);

-- Backfill des lots existants (s'il y en a) avec un numéro provisoire LTYYYY-historique_id
UPDATE lot_reglement
SET numero_lot = 'LT' || LPAD(historique_id::text, 3, '0') || '/' || EXTRACT(YEAR FROM created_at)::text
WHERE numero_lot IS NULL;

ALTER TABLE lot_reglement
    ALTER COLUMN numero_lot SET NOT NULL;

CREATE UNIQUE INDEX uk_lot_reglement_numero_lot ON lot_reglement(numero_lot);
