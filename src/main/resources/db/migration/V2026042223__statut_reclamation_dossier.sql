-- ============================================================
--  V23 — Statut de réclamation par dossier victime
-- ============================================================

-- 1. Nouveau type ENUM PostgreSQL (IF NOT EXISTS workaround)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_reclamation_enum') THEN
        CREATE TYPE statut_reclamation_enum AS ENUM (
            'BON_A_PAYER',
            'ARBITRAGE',
            'PROBLEME_RC',
            'ATTENTE_OFFRE',
            'ATTENTE_PIECES',
            'AUTRES',
            'CLOTURE'
        );
    END IF;
END$$;

-- 2. Ajout de la colonne sur dossier_reclamation
ALTER TABLE dossier_reclamation
ADD COLUMN IF NOT EXISTS statut_reclamation statut_reclamation_enum DEFAULT 'AUTRES';

-- Les dossiers déjà clôturés reçoivent CLOTURE
UPDATE dossier_reclamation
SET statut_reclamation = 'CLOTURE'
WHERE statut IN ('CLOTURE', 'REJETE');

COMMENT ON COLUMN dossier_reclamation.statut_reclamation IS 'Statut de réclamation par victime (indépendant du workflow interne).
        BON_A_PAYER : accord trouvé, bon à payer émis pour cette victime.
        ARBITRAGE   : dossier en arbitrage.
        PROBLEME_RC : problème de responsabilité civile identifié.
        ATTENTE_OFFRE : en attente de l''offre d''indemnisation.
        ATTENTE_PIECES : en attente de pièces justificatives.
        AUTRES      : tout autre statut en cours.
        CLOTURE     : dossier clos (payé ou sans suite).';