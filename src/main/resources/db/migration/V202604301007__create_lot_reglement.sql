-- V202604301007__create_lot_reglement.sql

CREATE TABLE lot_reglement (
    historique_id SERIAL PRIMARY KEY,
    lot_tracking_id UUID NOT NULL UNIQUE,
    expert_id INTEGER NOT NULL REFERENCES expert(historique_id),
    taux_retenue VARCHAR(30) NOT NULL,
    statut VARCHAR(40) NOT NULL,
    nombre_reglements INTEGER NOT NULL,
    montant_ttc_total NUMERIC(15,2) NOT NULL,
    montant_tva_total NUMERIC(15,2) NOT NULL,
    montant_taxe_total NUMERIC(15,2) NOT NULL,

    numero_cheque_global VARCHAR(30),
    banque_cheque VARCHAR(150),
    date_emission_cheque DATE,

    -- Colonnes héritées InternalHistorique
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(150),
    updated_by VARCHAR(150),
    deleted_by VARCHAR(150),
    libelle VARCHAR(255),
    active_data BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_data BOOLEAN NOT NULL DEFAULT FALSE,
    parent_code_id VARCHAR(255),
    from_table VARCHAR(50),
    excel BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_lot_reglement_expert_statut
    ON lot_reglement(expert_id, statut)
    WHERE active_data = TRUE AND deleted_data = FALSE;
