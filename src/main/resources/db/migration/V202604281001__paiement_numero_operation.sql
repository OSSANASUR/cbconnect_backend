-- =============================================================================
-- V202604281001 — Ajout du numero_operation sur paiement & prefinancement
-- Format : <TYPE>-<YYYY>-<NUM_SINISTRE>-<SEQ>
--   TYPE = RT | RC | PF | AN
--   YYYY = année 4 chiffres (created_at)
--   NUM_SINISTRE = numero_sinistre_local (fallback numero_sinistre_manuel,
--                  fallback ultime 'SIN<historique_id>'), sanitisé [A-Z0-9]
--   SEQ = compteur 3 chiffres **par (num_sinistre sanitisé, type)**
--   Note : la partition est faite sur le num_sinistre sanitisé (pas le
--   sinistre_id), car deux sinistres distincts peuvent produire le même
--   num_sin après sanitisation (ex. variations de saisie). Le compteur
--   est partagé entre eux pour garantir l'unicité globale du numero_operation.
-- =============================================================================

-- ── 1) Ajout colonnes nullable ───────────────────────────────────────────────
ALTER TABLE paiement
    ADD COLUMN numero_operation VARCHAR(30);

ALTER TABLE prefinancement
    ADD COLUMN numero_operation VARCHAR(30);

-- ── 2) Backfill paiement (RT / RC / AN) ──────────────────────────────────────
WITH typed AS (
    SELECT p.historique_id,
           CASE
               WHEN p.statut = 'ANNULE' THEN 'AN'
               WHEN p.statut IN ('REGLEMENT_COMPTABLE_VALIDE', 'PAYE') THEN 'RC'
               ELSE 'RT'
           END AS type_code,
           p.created_at,
           UPPER(REGEXP_REPLACE(
               COALESCE(NULLIF(sin.numero_sinistre_local, ''),
                        NULLIF(sin.numero_sinistre_manuel, ''),
                        'SIN' || sin.historique_id::text),
               '[^A-Za-z0-9]', '', 'g')) AS num_sin
    FROM paiement p
    JOIN sinistre sin ON sin.historique_id = p.sinistre_id
),
seq AS (
    SELECT t.historique_id, t.type_code, t.num_sin,
           EXTRACT(YEAR FROM t.created_at)::int AS yyyy,
           ROW_NUMBER() OVER (
               PARTITION BY t.num_sin, t.type_code
               ORDER BY t.created_at, t.historique_id
           ) AS seq
    FROM typed t
)
UPDATE paiement p
SET numero_operation = sa.type_code || '-' || sa.yyyy || '-' || sa.num_sin
                       || '-' || LPAD(sa.seq::text, 3, '0')
FROM seq sa
WHERE p.historique_id = sa.historique_id;

-- ── 3) Backfill prefinancement (PF, type unique) ─────────────────────────────
WITH typed AS (
    SELECT pf.historique_id,
           pf.created_at,
           UPPER(REGEXP_REPLACE(
               COALESCE(NULLIF(sin.numero_sinistre_local, ''),
                        NULLIF(sin.numero_sinistre_manuel, ''),
                        'SIN' || sin.historique_id::text),
               '[^A-Za-z0-9]', '', 'g')) AS num_sin
    FROM prefinancement pf
    JOIN sinistre sin ON sin.historique_id = pf.sinistre_id
),
seq AS (
    SELECT t.historique_id, t.num_sin,
           EXTRACT(YEAR FROM t.created_at)::int AS yyyy,
           ROW_NUMBER() OVER (
               PARTITION BY t.num_sin
               ORDER BY t.created_at, t.historique_id
           ) AS seq
    FROM typed t
)
UPDATE prefinancement pf
SET numero_operation = 'PF-' || sa.yyyy || '-' || sa.num_sin
                       || '-' || LPAD(sa.seq::text, 3, '0')
FROM seq sa
WHERE pf.historique_id = sa.historique_id;

-- ── 4) Verrouillage : NOT NULL + UNIQUE INDEX (les 2 tables) ─────────────────
ALTER TABLE paiement
    ALTER COLUMN numero_operation SET NOT NULL;
CREATE UNIQUE INDEX ux_paiement_numero_operation
    ON paiement (numero_operation);

ALTER TABLE prefinancement
    ALTER COLUMN numero_operation SET NOT NULL;
CREATE UNIQUE INDEX ux_prefinancement_numero_operation
    ON prefinancement (numero_operation);
