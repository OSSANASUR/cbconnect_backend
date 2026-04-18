-- V10__vues_reprise.sql

CREATE OR REPLACE VIEW v_reprise_statut AS
SELECT
    'sinistres'                                                      AS entite,
    COUNT(*)                                                         AS total_importe,
    COUNT(*) FILTER (WHERE type_sinistre = 'SURVENU_TOGO')          AS total_et,
    COUNT(*) FILTER (WHERE type_sinistre = 'SURVENU_ETRANGER')      AS total_te,
    MIN(date_accident)                                               AS premiere_date,
    MAX(date_accident)                                               AS derniere_date
FROM sinistre
WHERE reprise_historique = TRUE AND deleted_data = FALSE
UNION ALL
SELECT 'organismes', COUNT(*), 0, 0, NULL, NULL
FROM organisme
WHERE reprise_historique = TRUE AND deleted_data = FALSE;


CREATE OR REPLACE VIEW v_reprise_par_pays AS
SELECT
    p.code_iso,
    p.libelle                                                        AS pays,
    COUNT(s.historique_id)                                           AS nb_sinistres,
    COUNT(*) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO')        AS nb_et,
    COUNT(*) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER')    AS nb_te,
    COUNT(*) FILTER (WHERE s.type_dommage  = 'MATERIEL')            AS nb_materiel,
    COUNT(*) FILTER (WHERE s.type_dommage  = 'CORPOREL')            AS nb_corporel,
    COUNT(*) FILTER (WHERE s.type_dommage  = 'MIXTE')               AS nb_mixte
FROM sinistre s
JOIN pays p ON p.historique_id = s.pays_emetteur_id  -- ← historique_id
WHERE s.reprise_historique = TRUE AND s.deleted_data = FALSE
GROUP BY p.code_iso, p.libelle
ORDER BY nb_sinistres DESC;