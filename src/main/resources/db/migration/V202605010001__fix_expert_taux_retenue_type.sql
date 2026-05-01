ALTER TABLE expert ALTER COLUMN taux_retenue DROP DEFAULT;
ALTER TABLE expert ALTER COLUMN taux_retenue DROP NOT NULL;
ALTER TABLE expert
    ALTER COLUMN taux_retenue TYPE NUMERIC(5,2)
    USING CASE
        WHEN taux_retenue IS NULL OR taux_retenue = '' THEN NULL
        WHEN taux_retenue ~ '^-?[0-9]+(\.[0-9]+)?$' THEN taux_retenue::NUMERIC(5,2)
        ELSE NULL
    END;
