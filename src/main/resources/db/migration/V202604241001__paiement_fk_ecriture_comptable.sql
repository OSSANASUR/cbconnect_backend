-- Ajoute la FK ecriture_comptable_id sur la table paiement.
-- Nullable : les paiements issus de la reprise historique (V9__reprise_historique.sql)
-- n'ont pas d'écriture comptable associée.
-- FK vers ecriture_comptable.id (PK SERIAL, EcritureComptable n'hérite pas d'InternalHistorique).

ALTER TABLE paiement
    ADD COLUMN IF NOT EXISTS ecriture_comptable_id INTEGER
        REFERENCES ecriture_comptable(id);

CREATE INDEX IF NOT EXISTS idx_paiement_ecriture_comptable
    ON paiement (ecriture_comptable_id);
