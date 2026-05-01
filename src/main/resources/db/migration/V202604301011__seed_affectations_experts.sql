
-- V202604301011__seed_affectations_experts.sql
-- Affecte les experts (seedés en V202604301010) à des sinistres existants pour permettre
-- le test du flux "règlement multiple aux experts" :
--   * affectation_expert(statut='VALIDE')
--   * expertise_medicale OU expertise_materielle avec date_rapport renseignée
--
-- Cibles : sinistres actifs ayant déjà au moins un encaissement non-annulé (RÈGLE A
-- du module règlement). Limite à 3 sinistres par expert pour ne pas surcharger.
-- Idempotent (re-exécution sans doublon grâce à NOT EXISTS).

-- ─────────────────────────────────────────────────────────────────────────────
-- 1) Affectations : 1 expert ↔ 1 sinistre, statut VALIDE, victime principale du sinistre
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO affectation_expert (
    expert_id, victime_id, sinistre_id,
    type_expertise, date_affectation, date_limite_rapport, statut,
    created_at, created_by, libelle,
    active_data, deleted_data, excel, from_table,
    affectation_tracking_id
)
SELECT
    e.historique_id,
    v.historique_id,
    s.historique_id,
    'INITIALE',
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE - INTERVAL '5 days',
    'VALIDE',
    NOW(), 'SYSTEM',
    'Seed affectation — ' || e.nom_complet,
    TRUE, FALSE, FALSE, 'AFFECTATION_EXPERT',
    gen_random_uuid()
FROM expert e
CROSS JOIN LATERAL (
    SELECT s2.historique_id
    FROM sinistre s2
    WHERE s2.active_data = TRUE
      AND s2.deleted_data = FALSE
      AND EXISTS (
          SELECT 1 FROM encaissement enc
          WHERE enc.sinistre_id = s2.historique_id
            AND enc.statut_cheque <> 'ANNULE'
            AND enc.active_data = TRUE
            AND enc.deleted_data = FALSE
      )
      AND EXISTS (
          SELECT 1 FROM victime vv
          WHERE vv.sinistre_id = s2.historique_id
            AND vv.active_data = TRUE
            AND vv.deleted_data = FALSE
      )
    ORDER BY s2.historique_id
    LIMIT 3
) s
JOIN LATERAL (
    SELECT v2.historique_id
    FROM victime v2
    WHERE v2.sinistre_id = s.historique_id
      AND v2.active_data = TRUE
      AND v2.deleted_data = FALSE
    ORDER BY v2.historique_id
    LIMIT 1
) v ON TRUE
WHERE e.active_data = TRUE
  AND e.deleted_data = FALSE
  AND e.actif = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM affectation_expert ae
      WHERE ae.expert_id = e.historique_id
        AND ae.sinistre_id = s.historique_id
        AND ae.active_data = TRUE
        AND ae.deleted_data = FALSE
  );

-- ─────────────────────────────────────────────────────────────────────────────
-- 2) Expertises médicales : pour chaque expert MEDICAL, créer une expertise_medicale
--    avec date_rapport sur les sinistres affectés (via la victime).
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO expertise_medicale (
    type_expertise, date_demande, date_rapport, date_consolidation,
    taux_ipp, duree_itt_jours, duree_itp_jours,
    pretium_doloris, prejudice_esthetique, necessite_tierce_personne,
    honoraires, honoraires_contre_expertise,
    victime_id, expert_id,
    created_at, created_by, libelle,
    active_data, deleted_data, excel, from_table
)
SELECT
    'INITIALE',
    CURRENT_DATE - INTERVAL '25 days',
    CURRENT_DATE - INTERVAL '7 days',
    NULL,
    5.00, 30, 45,
    'NEANT', 'NEANT', FALSE,
    e.mont_expertise, 0,
    ae.victime_id, e.historique_id,
    NOW(), 'SYSTEM',
    'Seed expertise médicale',
    TRUE, FALSE, FALSE, 'EXPERTISE_MEDICALE'
FROM affectation_expert ae
JOIN expert e ON e.historique_id = ae.expert_id
WHERE e.type_expert = 'MEDICAL'
  AND ae.statut = 'VALIDE'
  AND ae.active_data = TRUE AND ae.deleted_data = FALSE
  AND e.active_data = TRUE AND e.deleted_data = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM expertise_medicale em
      WHERE em.expert_id = ae.expert_id
        AND em.victime_id = ae.victime_id
        AND em.active_data = TRUE
        AND em.deleted_data = FALSE
  );

-- ─────────────────────────────────────────────────────────────────────────────
-- 3) Expertises matérielles : pour chaque expert AUTOMOBILE, créer une
--    expertise_materielle avec date_rapport sur les sinistres affectés.
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO expertise_materielle (
    date_demande, date_rapport,
    montant_devis, montant_dit_expert, honoraires,
    sinistre_id, expert_id,
    created_at, created_by, libelle,
    active_data, deleted_data, excel, from_table
)
SELECT
    CURRENT_DATE - INTERVAL '25 days',
    CURRENT_DATE - INTERVAL '7 days',
    250000.00, 200000.00, e.mont_expertise,
    ae.sinistre_id, e.historique_id,
    NOW(), 'SYSTEM',
    'Seed expertise matérielle',
    TRUE, FALSE, FALSE, 'EXPERTISE_MATERIELLE'
FROM affectation_expert ae
JOIN expert e ON e.historique_id = ae.expert_id
WHERE e.type_expert = 'AUTOMOBILE'
  AND ae.statut = 'VALIDE'
  AND ae.active_data = TRUE AND ae.deleted_data = FALSE
  AND e.active_data = TRUE AND e.deleted_data = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM expertise_materielle em
      WHERE em.expert_id = ae.expert_id
        AND em.sinistre_id = ae.sinistre_id
        AND em.active_data = TRUE
        AND em.deleted_data = FALSE
  );
