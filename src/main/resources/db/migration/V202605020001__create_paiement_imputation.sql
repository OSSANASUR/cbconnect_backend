-- Imputation explicite paiement → encaissement avec montant alloué
-- Pattern contre-passation : montant_impute signé (+création / −annulation)
CREATE TABLE paiement_imputation (
    historique_id            BIGSERIAL PRIMARY KEY,
    imputation_tracking_id   UUID NOT NULL UNIQUE,
    paiement_id              BIGINT NOT NULL REFERENCES paiement(historique_id),
    encaissement_id          BIGINT NOT NULL REFERENCES encaissement(historique_id),
    montant_impute           NUMERIC(15,2) NOT NULL,
    imputation_origine_id    BIGINT REFERENCES paiement_imputation(historique_id),
    active_data              BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_data             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by               VARCHAR(100),
    updated_at               TIMESTAMP,
    updated_by               VARCHAR(100),
    deleted_at               TIMESTAMP,
    deleted_by               VARCHAR(100),
    from_table               VARCHAR(50) DEFAULT 'PAIEMENT_IMPUTATION'
);

CREATE INDEX idx_pi_enc_actif ON paiement_imputation(encaissement_id)
  WHERE active_data = TRUE AND deleted_data = FALSE;

CREATE INDEX idx_pi_paiement_actif ON paiement_imputation(paiement_id)
  WHERE active_data = TRUE AND deleted_data = FALSE;

COMMENT ON COLUMN paiement_imputation.montant_impute IS
  'Signé : positif = imputation, négatif = contre-passage rattaché à un AN';
COMMENT ON COLUMN paiement_imputation.imputation_origine_id IS
  'Pour les contre-passages : pointe vers la ligne d''origine positive';
