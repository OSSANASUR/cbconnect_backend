-- CBConnect V22 — Correction sémantique pays : cas dégénérés laissés par V21
--
-- V21 n'inversait que les lignes où pays_gestionnaire_id = TG ET pays_emetteur_id <> TG
-- (signal clair d'inversion). Elle ne traitait pas :
--   - les lignes où pays_gestionnaire_id = pays_emetteur_id (les deux colonnes
--     portent la même valeur issue de l'import) ;
--   - les sinistres ET (SURVENU_TOGO) dont le gestionnaire n'est pas TG.
--
-- Règle métier rappelée :
--   TE (SURVENU_ETRANGER)  : emetteur     = TG (BNCB-TG a émis la carte brune)
--                            gestionnaire = pays étranger où l'accident a eu lieu
--   ET (SURVENU_TOGO)      : gestionnaire = TG (accident au Togo)
--                            emetteur     = pays étranger émetteur de la carte
--
-- Cette migration ne touche que les sinistres de reprise (reprise_historique = true).

DO $$
DECLARE
    tg_id INT;
BEGIN
    SELECT historique_id INTO tg_id FROM pays
     WHERE (code_iso = 'TG' OR code_carte_brune = 'TG') AND active_data = true
     LIMIT 1;

    IF tg_id IS NULL THEN
        RAISE NOTICE 'Pays TG introuvable — V22 ne modifie rien';
        RETURN;
    END IF;

    -- 1) TE : l'émetteur doit toujours être TG.
    --    Couvre notamment les (X, X) où X <> TG : on force emetteur = TG sans
    --    toucher au gestionnaire (qui reste le pays étranger).
    UPDATE sinistre
       SET pays_emetteur_id = tg_id,
           updated_at       = NOW(),
           updated_by       = COALESCE(updated_by, 'SYSTEM-V22')
     WHERE type_sinistre       = 'SURVENU_ETRANGER'
       AND reprise_historique  = true
       AND pays_emetteur_id   IS DISTINCT FROM tg_id
       AND pays_gestionnaire_id IS NOT NULL
       AND pays_gestionnaire_id <> tg_id
       AND active_data         = true
       AND deleted_data        = false;

    -- 2) ET : le gestionnaire doit toujours être TG.
    --    Couvre (X, X), (X, TG) et (X, Y) où X <> TG : on force gestionnaire = TG
    --    sans toucher à l'émetteur (qui reste le pays étranger émetteur).
    UPDATE sinistre
       SET pays_gestionnaire_id = tg_id,
           updated_at           = NOW(),
           updated_by           = COALESCE(updated_by, 'SYSTEM-V22')
     WHERE type_sinistre         = 'SURVENU_TOGO'
       AND reprise_historique    = true
       AND pays_gestionnaire_id IS DISTINCT FROM tg_id
       AND pays_emetteur_id     IS NOT NULL
       AND active_data           = true
       AND deleted_data          = false;

    -- 3) Cas ambigus (gestionnaire = TG ET émetteur = TG) : on ne peut pas
    --    deviner le vrai pays, on les marque pour revue manuelle.
    UPDATE sinistre
       SET updated_at = NOW(),
           updated_by = 'V22-REVIEW'
     WHERE reprise_historique   = true
       AND pays_gestionnaire_id = tg_id
       AND pays_emetteur_id     = tg_id
       AND active_data          = true
       AND deleted_data         = false;
END $$;
