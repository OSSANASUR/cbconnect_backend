-- V202604301010__seed_experts.sql
-- Seed initial d'experts (médicaux + automobiles) pour démo / tests d'intégration.

INSERT INTO expert (
    expert_tracking_id, type_expert, nom_complet, specialite, nif,
    email, telephone,
    taux_retenue, mont_expertise, actif,
    pays_id,
    created_at, created_by, libelle, active_data, deleted_data, excel, from_table
)
SELECT * FROM (VALUES
    (gen_random_uuid(), 'MEDICAL',    'Dr. KOUAME Pierre',        'Médecin légiste',        'NIF-MED-001',
        'p.kouame@experts.tg',   '+228 90 11 22 01',
        'CINQ_POURCENT',  40000.00, TRUE,
        (SELECT historique_id FROM pays WHERE code_iso = 'TGO' LIMIT 1),
        NOW(), 'SYSTEM', 'Dr. KOUAME Pierre',  TRUE, FALSE, FALSE, 'EXPERT'),

    (gen_random_uuid(), 'MEDICAL',    'Dr. AGBOZO Marie',         'Orthopédiste',           'NIF-MED-002',
        'm.agbozo@experts.tg',   '+228 90 11 22 02',
        'CINQ_POURCENT',  50000.00, TRUE,
        (SELECT historique_id FROM pays WHERE code_iso = 'TGO' LIMIT 1),
        NOW(), 'SYSTEM', 'Dr. AGBOZO Marie',   TRUE, FALSE, FALSE, 'EXPERT'),

    (gen_random_uuid(), 'MEDICAL',    'Dr. BAKARI Issouf',        'Neurologie',             'NIF-MED-003',
        'i.bakari@experts.tg',   '+228 90 11 22 03',
        'TROIS_POURCENT', 60000.00, TRUE,
        (SELECT historique_id FROM pays WHERE code_iso = 'TGO' LIMIT 1),
        NOW(), 'SYSTEM', 'Dr. BAKARI Issouf',  TRUE, FALSE, FALSE, 'EXPERT'),

    (gen_random_uuid(), 'AUTOMOBILE', 'M. SOSSOU Jean',           'Carrosserie / VEI',      'NIF-AUTO-001',
        'j.sossou@experts.tg',   '+228 90 33 44 01',
        'CINQ_POURCENT',  40000.00, TRUE,
        (SELECT historique_id FROM pays WHERE code_iso = 'TGO' LIMIT 1),
        NOW(), 'SYSTEM', 'M. SOSSOU Jean',     TRUE, FALSE, FALSE, 'EXPERT'),

    (gen_random_uuid(), 'AUTOMOBILE', 'M. ATTIOGBE Komla',        'Mécanique générale',     'NIF-AUTO-002',
        'k.attiogbe@experts.tg', '+228 90 33 44 02',
        'CINQ_POURCENT',  45000.00, TRUE,
        (SELECT historique_id FROM pays WHERE code_iso = 'TGO' LIMIT 1),
        NOW(), 'SYSTEM', 'M. ATTIOGBE Komla',  TRUE, FALSE, FALSE, 'EXPERT'),

    (gen_random_uuid(), 'AUTOMOBILE', 'Cabinet AUTOEXPERT Lomé',  'Cabinet d''expertise',   'NIF-AUTO-003',
        'contact@autoexpert.tg', '+228 22 25 30 10',
        'VINGT_POURCENT', 75000.00, TRUE,
        (SELECT historique_id FROM pays WHERE code_iso = 'TGO' LIMIT 1),
        NOW(), 'SYSTEM', 'Cabinet AUTOEXPERT', TRUE, FALSE, FALSE, 'EXPERT')
) AS v(
    expert_tracking_id, type_expert, nom_complet, specialite, nif,
    email, telephone,
    taux_retenue, mont_expertise, actif,
    pays_id,
    created_at, created_by, libelle, active_data, deleted_data, excel, from_table
)
WHERE NOT EXISTS (SELECT 1 FROM expert WHERE expert.nif = v.nif);
