-- =============================================================================
-- V202604291001 — Workflow Préfinancement
--   1) Assouplit numero_operation (NULL pendant statut DEMANDE, rempli à VALIDE)
--   2) Ajout des colonnes de workflow (statut, motifs, validation, annulation, FK écriture)
--   3) Création de la table prefinancement_remboursement (multi-imputations)
-- =============================================================================

-- 1) numero_operation peut être NULL en DEMANDE
ALTER TABLE prefinancement
    ALTER COLUMN numero_operation DROP NOT NULL;

-- 2) Colonnes workflow
ALTER TABLE prefinancement
    ADD COLUMN statut VARCHAR(30) NOT NULL DEFAULT 'VALIDE',
    ADD COLUMN motif_demande VARCHAR(500),
    ADD COLUMN motif_annulation VARCHAR(500),
    ADD COLUMN valide_par_id INTEGER REFERENCES utilisateur(historique_id),
    ADD COLUMN date_validation TIMESTAMPTZ,
    ADD COLUMN annule_par_id INTEGER REFERENCES utilisateur(historique_id),
    ADD COLUMN ecriture_comptable_id INTEGER REFERENCES ecriture_comptable(id);

-- 3) Table de jointure remboursements
CREATE TABLE prefinancement_remboursement (
    historique_id SERIAL PRIMARY KEY,
    remboursement_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    prefinancement_id INTEGER NOT NULL REFERENCES prefinancement(historique_id),
    encaissement_source_id INTEGER NOT NULL REFERENCES encaissement(historique_id),
    montant DECIMAL(15,2) NOT NULL CHECK (montant > 0),
    date_remboursement DATE NOT NULL,
    valide_par_id INTEGER REFERENCES utilisateur(historique_id),
    ecriture_comptable_id INTEGER REFERENCES ecriture_comptable(id),
    -- audit InternalHistorique
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ,
    created_by VARCHAR(150), updated_by VARCHAR(150), deleted_by VARCHAR(150),
    libelle VARCHAR(255), active_data BOOLEAN NOT NULL DEFAULT true,
    parent_code_id VARCHAR(255), deleted_data BOOLEAN NOT NULL DEFAULT false,
    from_table VARCHAR(50) DEFAULT 'PREFINANCEMENT_REMBOURSEMENT', excel BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_prefin_remb_prefin
    ON prefinancement_remboursement(prefinancement_id)
    WHERE active_data = true AND deleted_data = false;

CREATE INDEX idx_prefin_remb_encaissement
    ON prefinancement_remboursement(encaissement_source_id)
    WHERE active_data = true AND deleted_data = false;
