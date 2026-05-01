-- No-op : cette migration tentait de re-convertir expert.taux_retenue en NUMERIC,
-- ce qui est incompatible avec l'entité Java (enum TauxRetenue stocké en VARCHAR(30)).
-- La colonne reste VARCHAR(30) tel que défini par V202604301005.
SELECT 1;
