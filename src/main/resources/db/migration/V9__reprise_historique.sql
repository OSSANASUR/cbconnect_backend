-- ============================================================
--  V8 — Reprise historique BNCB Togo 2025
--  Ajout des champs nécessaires à la reprise des sinistres
--  et organismes historiques
-- ============================================================

-- ── Champ reprise sur la table sinistre ──────────────────────────────────
ALTER TABLE sinistre
    ADD COLUMN IF NOT EXISTS reprise_historique   BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS assureur_declarant   VARCHAR(200),
    ADD COLUMN IF NOT EXISTS numero_police_assureur VARCHAR(200);

COMMENT ON COLUMN sinistre.reprise_historique   IS 'TRUE si importé via reprise historique';
COMMENT ON COLUMN sinistre.assureur_declarant   IS 'Compagnie déclarante (reprise) ex: SUNU BJ';
COMMENT ON COLUMN sinistre.numero_police_assureur IS 'N° police chez l''assureur émetteur';

-- ── Index unicité sur numeroSinistreManuel ─────────────────────────────
-- Évite les doublons lors de reprises multiples
CREATE UNIQUE INDEX IF NOT EXISTS uidx_sinistre_numero_manuel
    ON sinistre (numero_sinistre_manuel)
    WHERE numero_sinistre_manuel IS NOT NULL;

-- ── Champ reprise sur la table organisme ─────────────────────────────────
ALTER TABLE organisme
    ADD COLUMN IF NOT EXISTS reprise_historique BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN organisme.reprise_historique IS 'TRUE si créé lors de l''import compagnies CEDEAO';

-- ── Vue statistique rapide de la reprise ─────────────────────────────────
CREATE OR REPLACE VIEW v_reprise_statut AS
SELECT
    'sinistres'                                                 AS entite,
    COUNT(*)                                                    AS total_importe,
    COUNT(*) FILTER (WHERE type_sinistre = 'SURVENU_TOGO')     AS total_et,
    COUNT(*) FILTER (WHERE type_sinistre = 'SURVENU_ETRANGER') AS total_te,
    MIN(date_accident)                                          AS premiere_date,
    MAX(date_accident)                                          AS derniere_date
FROM sinistre
WHERE reprise_historique = TRUE
  AND deleted_data = FALSE

UNION ALL

SELECT
    'organismes',
    COUNT(*),
    0, 0,
    NULL, NULL
FROM organisme
WHERE reprise_historique = TRUE
  AND deleted_data = FALSE;

-- ── Rapport de reprise par pays (pour vérification post-import) ──────────
CREATE OR REPLACE VIEW v_reprise_par_pays AS
SELECT
    p.code_iso                              AS pays_code,
    p.libelle                               AS pays,
    COUNT(s.sinistre_id)                    AS nb_sinistres,
    COUNT(*) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO')     AS nb_et,
    COUNT(*) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER') AS nb_te,
    COUNT(*) FILTER (WHERE s.type_dommage  = 'MATERIEL')         AS nb_materiel,
    COUNT(*) FILTER (WHERE s.type_dommage  = 'CORPOREL')         AS nb_corporel,
    COUNT(*) FILTER (WHERE s.type_dommage  = 'MIXTE')            AS nb_mixte
FROM sinistre s
JOIN pays p ON p.pays_id = s.pays_emetteur_id
WHERE s.reprise_historique = TRUE
  AND s.deleted_data = FALSE
GROUP BY p.code_iso, p.libelle
ORDER BY nb_sinistres DESC;
