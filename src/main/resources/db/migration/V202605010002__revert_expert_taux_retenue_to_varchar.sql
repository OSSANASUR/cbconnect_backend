-- V202605010002 — Revert expert.taux_retenue from NUMERIC back to VARCHAR(30) (TauxRetenue enum)
-- V202604301005 had already converted this column to varchar; V202605010001 incorrectly reverted it.
ALTER TABLE expert ALTER COLUMN taux_retenue DROP DEFAULT;
ALTER TABLE expert ALTER COLUMN taux_retenue DROP NOT NULL;
ALTER TABLE expert
    ALTER COLUMN taux_retenue TYPE VARCHAR(30)
    USING CASE
        WHEN taux_retenue IS NULL     THEN 'CINQ_POURCENT'
        WHEN taux_retenue = 0.03      THEN 'TROIS_POURCENT'
        WHEN taux_retenue = 0.05      THEN 'CINQ_POURCENT'
        WHEN taux_retenue = 0.20      THEN 'VINGT_POURCENT'
        ELSE                               'CINQ_POURCENT'
    END;
UPDATE expert SET taux_retenue = 'CINQ_POURCENT' WHERE taux_retenue IS NULL;
ALTER TABLE expert ALTER COLUMN taux_retenue SET NOT NULL;
ALTER TABLE expert ALTER COLUMN taux_retenue SET DEFAULT 'CINQ_POURCENT';
