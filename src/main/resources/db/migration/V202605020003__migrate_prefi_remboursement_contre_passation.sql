-- Migrer les remboursements préfi historiquement annulés (active_data=FALSE)
-- vers le pattern contre-passation : créer une ligne négative, remettre l'origine active.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT * FROM prefinancement_remboursement
        WHERE active_data = FALSE
          AND remboursement_origine_id IS NULL  -- pas déjà migré
    LOOP
        -- Créer la contre-passation
        INSERT INTO prefinancement_remboursement (
            remboursement_tracking_id,
            prefinancement_id, encaissement_source_id,
            montant, date_remboursement,
            valide_par_id, ecriture_comptable_id,
            remboursement_origine_id,
            active_data, deleted_data,
            created_at, created_by,
            from_table
        ) VALUES (
            gen_random_uuid(),
            r.prefinancement_id, r.encaissement_source_id,
            -r.montant, r.date_remboursement,
            r.valide_par_id, NULL,
            r.historique_id,
            TRUE, FALSE,
            COALESCE(r.deleted_at, NOW()), COALESCE(r.deleted_by, 'system-migration'),
            'PREFINANCEMENT_REMBOURSEMENT'
        );

        -- Réactiver l'origine
        UPDATE prefinancement_remboursement
           SET active_data = TRUE,
               deleted_data = FALSE,
               deleted_at = NULL,
               deleted_by = NULL
         WHERE historique_id = r.historique_id;
    END LOOP;
END $$;
