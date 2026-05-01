-- V202604301012__seed_victimes_sinistre_0482.sql
-- Ajoute 3 victimes au sinistre "2025/0482/BJ" (matché sur l'un des numéros).
-- Idempotent : pas de doublon si même nom + prénoms + sinistre déjà présents.

INSERT INTO victime (
    victime_tracking_id, nom, prenoms, date_naissance, sexe, nationalite,
    type_victime, statut_victime, statut_activite, revenu_mensuel,
    est_dcd_suite_blessures, lien_deces_accident,
    sinistre_id, pays_residence_id,
    created_at, created_by, libelle,
    active_data, deleted_data, excel, from_table
)
SELECT
    v.victime_tracking_id, v.nom, v.prenoms, v.date_naissance, v.sexe, v.nationalite,
    v.type_victime, v.statut_victime, v.statut_activite, v.revenu_mensuel,
    v.est_dcd_suite_blessures, v.lien_deces_accident,
    target.sinistre_id, p.pays_residence_id,
    NOW(), 'SYSTEM', v.libelle,
    TRUE, FALSE, FALSE, 'VICTIME'
FROM (VALUES
    (gen_random_uuid(), 'GNANSOUNOU', 'Bertin',  DATE '1985-03-12', 'M', 'Béninoise',
     'BLESSE', 'EN_COURS_INDEMNISATION', 'ACTIF',           150000.00,
     FALSE, FALSE,
     'GNANSOUNOU Bertin'),

    (gen_random_uuid(), 'KPONOU',     'Adjoa',   DATE '1992-07-25', 'F', 'Togolaise',
     'BLESSE', 'EN_COURS_INDEMNISATION', 'ACTIF',            90000.00,
     FALSE, FALSE,
     'KPONOU Adjoa'),

    (gen_random_uuid(), 'AMOUSSOU',   'Edem',    DATE '2010-11-04', 'M', 'Togolaise',
     'BLESSE', 'EN_COURS_INDEMNISATION', 'ELEVE_ETUDIANT',        0.00,
     FALSE, FALSE,
     'AMOUSSOU Edem')
) AS v(
    victime_tracking_id, nom, prenoms, date_naissance, sexe, nationalite,
    type_victime, statut_victime, statut_activite, revenu_mensuel,
    est_dcd_suite_blessures, lien_deces_accident,
    libelle
)
CROSS JOIN LATERAL (
    SELECT s.historique_id AS sinistre_id
    FROM sinistre s
    WHERE s.active_data = TRUE
      AND s.deleted_data = FALSE
      AND (
          s.numero_sinistre_local     = '2025/0482/BJ'
       OR s.numero_sinistre_manuel    = '2025/0482/BJ'
       OR s.numero_sinistre_homologue = '2025/0482/BJ'
      )
    LIMIT 1
) target
LEFT JOIN LATERAL (
    SELECT historique_id AS pays_residence_id
    FROM pays
    WHERE code_iso = 'TGO'
    LIMIT 1
) p ON TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM victime existing
    WHERE existing.sinistre_id = target.sinistre_id
      AND existing.nom = v.nom
      AND existing.prenoms = v.prenoms
      AND existing.active_data = TRUE
      AND existing.deleted_data = FALSE
);
