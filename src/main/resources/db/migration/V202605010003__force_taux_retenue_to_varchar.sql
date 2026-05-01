-- Force la conversion de expert.taux_retenue en VARCHAR(30) pour matcher l'enum TauxRetenue.
-- V202604301005 était censée le faire mais n'a pas joué sur cet environnement.
-- Idempotent : ne fait rien si la colonne est déjà VARCHAR.
DO $$
DECLARE
    col_type text;
BEGIN
    SELECT data_type INTO col_type
    FROM information_schema.columns
    WHERE table_name = 'expert' AND column_name = 'taux_retenue';

    IF col_type = 'numeric' THEN
        ALTER TABLE expert ALTER COLUMN taux_retenue DROP DEFAULT;
        ALTER TABLE expert ALTER COLUMN taux_retenue DROP NOT NULL;

        ALTER TABLE expert
            ALTER COLUMN taux_retenue TYPE VARCHAR(30)
            USING CASE
                WHEN taux_retenue IS NULL THEN 'CINQ_POURCENT'
                WHEN taux_retenue = 0.03  THEN 'TROIS_POURCENT'
                WHEN taux_retenue = 0.05  THEN 'CINQ_POURCENT'
                WHEN taux_retenue = 0.20  THEN 'VINGT_POURCENT'
                ELSE 'CINQ_POURCENT'
            END;

        UPDATE expert SET taux_retenue = 'CINQ_POURCENT' WHERE taux_retenue IS NULL;
        ALTER TABLE expert ALTER COLUMN taux_retenue SET NOT NULL;
        ALTER TABLE expert ALTER COLUMN taux_retenue SET DEFAULT 'CINQ_POURCENT';
    END IF;
END $$;
