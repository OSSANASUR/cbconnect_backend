-- V202604301008__paiement_lot_ttc_tva_taxe.sql

ALTER TABLE paiement
    ADD COLUMN lot_reglement_id INTEGER,
    ADD COLUMN montant_ttc NUMERIC(15,2),
    ADD COLUMN montant_tva NUMERIC(15,2),
    ADD COLUMN montant_taxe NUMERIC(15,2);

ALTER TABLE paiement
    ADD CONSTRAINT fk_paiement_lot_reglement
    FOREIGN KEY (lot_reglement_id) REFERENCES lot_reglement(historique_id);

CREATE INDEX idx_paiement_lot
    ON paiement(lot_reglement_id)
    WHERE lot_reglement_id IS NOT NULL;
