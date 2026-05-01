-- V202604301005__expert_montexpertise_tauxretenue.sql

ALTER TABLE expert ADD COLUMN mont_expertise NUMERIC(15,2);

UPDATE expert SET mont_expertise = 40000 WHERE mont_expertise IS NULL;

ALTER TABLE expert ALTER COLUMN mont_expertise SET NOT NULL;
ALTER TABLE expert ALTER COLUMN mont_expertise SET DEFAULT 40000;

-- Conversion taux_retenue NUMERIC -> VARCHAR enum (TauxRetenue)
ALTER TABLE expert ALTER COLUMN taux_retenue TYPE VARCHAR(30)
    USING (CASE
        WHEN taux_retenue = 0.03 THEN 'TROIS_POURCENT'
        WHEN taux_retenue = 0.05 THEN 'CINQ_POURCENT'
        WHEN taux_retenue = 0.20 THEN 'VINGT_POURCENT'
        ELSE 'CINQ_POURCENT'
    END);

UPDATE expert SET taux_retenue = 'CINQ_POURCENT' WHERE taux_retenue IS NULL;

ALTER TABLE expert ALTER COLUMN taux_retenue SET DEFAULT 'CINQ_POURCENT';
ALTER TABLE expert ALTER COLUMN taux_retenue SET NOT NULL;
