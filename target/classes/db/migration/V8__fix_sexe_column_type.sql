-- Convertit les colonnes `sexe` de CHAR(1) vers VARCHAR(1) pour coller au mapping JPA (String + length=1 → VARCHAR).
ALTER TABLE victime     ALTER COLUMN sexe TYPE VARCHAR(1) USING sexe::VARCHAR(1);
ALTER TABLE ayant_droit ALTER COLUMN sexe TYPE VARCHAR(1) USING sexe::VARCHAR(1);
