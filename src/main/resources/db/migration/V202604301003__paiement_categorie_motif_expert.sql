ALTER TABLE paiement
    ADD COLUMN categorie VARCHAR(30),
    ADD COLUMN motif VARCHAR(150),
    ADD COLUMN beneficiaire_expert_id INTEGER;

ALTER TABLE paiement
    ADD CONSTRAINT fk_paiement_beneficiaire_expert
    FOREIGN KEY (beneficiaire_expert_id) REFERENCES expert(historique_id);

UPDATE paiement SET categorie = 'PRINCIPAL'         WHERE categorie IS NULL;
UPDATE paiement SET motif     = 'Reprise historique' WHERE motif IS NULL;

ALTER TABLE paiement
    ALTER COLUMN categorie SET NOT NULL,
    ALTER COLUMN motif     SET NOT NULL;

CREATE INDEX idx_paiement_sinistre_categorie ON paiement(sinistre_id, categorie);