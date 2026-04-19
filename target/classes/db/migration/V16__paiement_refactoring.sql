-- ============================================================
--  V14 — Refactoring Paiement
--
--  1. Remplacement du FK encaissement_id (Many-to-One)
--     par une table de jointure paiement_encaissement (Many-to-Many)
--  2. Ajout mode_paiement sur paiement
--  3. Ajout reprise_historique sur paiement
--  4. beneficiaire_victime_id et banque_cheque passent nullable
--     (nécessaire pour la reprise et les bénéficiaires organismes)
-- ============================================================

-- ── 1. Table de jointure paiement ↔ encaissement ─────────────────
CREATE TABLE IF NOT EXISTS paiement_encaissement (
    paiement_id     INT NOT NULL REFERENCES paiement(historique_id),
    encaissement_id INT NOT NULL REFERENCES encaissement(historique_id),
    PRIMARY KEY (paiement_id, encaissement_id)
);

COMMENT ON TABLE paiement_encaissement IS
    'Un paiement peut être issu de plusieurs encaissements (ex: virements groupés). '
    'Un encaissement peut générer plusieurs paiements (plusieurs bénéficiaires).';

-- ── 2. Migrer les liens existants vers la nouvelle table ──────────
INSERT INTO paiement_encaissement (paiement_id, encaissement_id)
SELECT historique_id, encaissement_id
FROM paiement
WHERE encaissement_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- ── 3. Supprimer l'ancienne FK (remplacée par la join table) ──────
ALTER TABLE paiement DROP COLUMN IF EXISTS encaissement_id;

-- ── 4. Nouveaux champs sur paiement ──────────────────────────────
ALTER TABLE paiement
    ADD COLUMN IF NOT EXISTS mode_paiement      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS reprise_historique BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN paiement.mode_paiement      IS 'CHEQUE | VIREMENT';
COMMENT ON COLUMN paiement.reprise_historique IS 'TRUE si importé via reprise historique';

-- ── 5. Assouplissement des contraintes pour la reprise ────────────
-- beneficiaire_victime_id : peut être null pour les bénéficiaires organismes
ALTER TABLE paiement ALTER COLUMN beneficiaire_victime_id DROP NOT NULL;

-- banque_cheque : non renseigné dans les fichiers historiques
ALTER TABLE paiement ALTER COLUMN banque_cheque DROP NOT NULL;
