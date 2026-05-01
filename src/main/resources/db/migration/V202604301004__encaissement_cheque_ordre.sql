ALTER TABLE encaissement
    ADD COLUMN cheque_ordre_organisme_id INTEGER;

ALTER TABLE encaissement
    ADD CONSTRAINT fk_encaissement_cheque_ordre_organisme
    FOREIGN KEY (cheque_ordre_organisme_id) REFERENCES organisme(historique_id);

-- Garde-fou : exactement 1 BNCB attendu
DO $$
DECLARE bncb_count INT;
BEGIN
    SELECT COUNT(*) INTO bncb_count
    FROM organisme
    WHERE type_organisme = 'BUREAU_NATIONAL'
      AND active_data = TRUE
      AND deleted_data = FALSE;

    IF bncb_count <> 1 THEN
        RAISE EXCEPTION 'Migration impossible : % organisme(s) BUREAU_NATIONAL trouvé(s), attendu : 1', bncb_count;
    END IF;
END $$;

UPDATE encaissement
SET cheque_ordre_organisme_id = (
    SELECT historique_id FROM organisme
    WHERE type_organisme = 'BUREAU_NATIONAL'
      AND active_data = TRUE AND deleted_data = FALSE
    LIMIT 1
)
WHERE cheque_ordre_organisme_id IS NULL;

ALTER TABLE encaissement
    ALTER COLUMN cheque_ordre_organisme_id SET NOT NULL;