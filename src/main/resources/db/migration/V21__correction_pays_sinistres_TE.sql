-- CBConnect V21 — Correction sémantique pays sur les sinistres TE
--
-- Contexte :
-- Jusqu'à la V20, l'import de reprise pour les sinistres SURVENU_ETRANGER (TE)
-- écrivait systématiquement paysGestionnaire = TG, ce qui contredit la
-- définition métier :
--   pays gestionnaire = pays où l'accident a eu lieu
--   pays émetteur     = pays qui a émis la carte brune
--
-- Sémantique correcte pour un TE :
--   paysGestionnaire = pays étranger où l'accident a eu lieu
--   paysEmetteur     = TG (BNCB-TG a émis la carte brune du Togolais assuré)
--
-- Cette migration corrige uniquement les sinistres importés par reprise
-- (repriseHistorique = true) de type TE pour lesquels la sémantique est
-- inversée (gestionnaire=TG, émetteur=étranger), afin de ne pas toucher
-- aux saisies manuelles faites après la correction.

DO $$
DECLARE
    tg_id INT;
BEGIN
    SELECT historique_id INTO tg_id FROM pays
     WHERE (code_iso = 'TG' OR code_carte_brune = 'TG') AND active_data = true
     LIMIT 1;

    IF tg_id IS NULL THEN
        RAISE NOTICE 'Pays TG introuvable — V21 ne modifie rien';
        RETURN;
    END IF;

    -- Inversion uniquement sur les TE de reprise où l'inversion est avérée
    UPDATE sinistre
       SET pays_gestionnaire_id = pays_emetteur_id,
           pays_emetteur_id     = tg_id,
           updated_at           = NOW(),
           updated_by           = COALESCE(updated_by, 'SYSTEM-V21')
     WHERE type_sinistre           = 'SURVENU_ETRANGER'
       AND reprise_historique      = true
       AND pays_gestionnaire_id    = tg_id
       AND pays_emetteur_id        IS NOT NULL
       AND pays_emetteur_id        <> tg_id
       AND active_data             = true
       AND deleted_data            = false;
END $$;

-- Recalage facultatif des homologues : non fait ici automatiquement car le
-- mapping pays → BCB-XX dépend de la table bureau_homologue ; un utilitaire
-- batch côté service (bouton "Recaler homologues") peut l'exécuter à la
-- demande si besoin.
