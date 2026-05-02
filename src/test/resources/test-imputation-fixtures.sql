-- ═══════════════════════════════════════════════════════════════════════════════
-- Fixtures pour PaiementImputationIntegrationTest (D4)
-- Toutes les lignes utilisent des UUIDs constants pour être référençables depuis
-- le test Java.  Le test est @Transactional + @Rollback : tout est annulé après.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── Assure minimal (pas de FK obligatoire hors données) ───────────────────────
INSERT INTO assure (
    assure_tracking_id, nom_assure, nom_complet,
    active_data, deleted_data, from_table, created_at, created_by
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'ASSURE-TEST', 'ASSURE-TEST INTEGRATION',
    true, false, 'ASSURE', NOW(), 'test-setup'
);

-- ── Sinistre de test ──────────────────────────────────────────────────────────
INSERT INTO sinistre (
    sinistre_tracking_id, numero_sinistre_local,
    type_sinistre, statut, type_dommage,
    date_accident, date_declaration,
    pays_gestionnaire_id, assure_id,
    active_data, deleted_data, from_table, created_at, created_by
)
SELECT
    's0000000-0000-0000-0000-000000000001',
    'TEST-D4-IMPUTATION-001',
    'SURVENU_TOGO', 'NOUVEAU', 'MATERIEL',
    '2024-01-15', '2024-01-20',
    p.historique_id,
    (SELECT historique_id FROM assure WHERE assure_tracking_id = 'a0000000-0000-0000-0000-000000000001'),
    true, false, 'SINISTRE', NOW(), 'test-setup'
FROM pays p
WHERE p.code_iso = 'TGO'
  AND p.active_data = true
  AND p.deleted_data = false
LIMIT 1;

-- ── Encaissement E1 : 50 000 FCFA ────────────────────────────────────────────
INSERT INTO encaissement (
    encaissement_tracking_id, numero_cheque,
    montant_cheque, montant_theorique,
    date_emission, date_encaissement,
    statut_cheque,
    organisme_emetteur_id, cheque_ordre_organisme_id,
    sinistre_id,
    active_data, deleted_data, from_table, created_at, created_by
)
SELECT
    'e1000000-0000-0000-0000-000000000001',
    'CHQ-D4-TEST-001',
    50000.00, 50000.00,
    '2024-01-25', '2024-02-01',
    'ENCAISSE',
    o.historique_id, o.historique_id,
    (SELECT historique_id FROM sinistre WHERE sinistre_tracking_id = 's0000000-0000-0000-0000-000000000001'),
    true, false, 'ENCAISSEMENT', NOW(), 'test-setup'
FROM organisme o
WHERE o.type_organisme = 'BUREAU_NATIONAL'
  AND o.active_data = true
  AND o.deleted_data = false
LIMIT 1;

-- ── Encaissement E2 : 50 000 FCFA ────────────────────────────────────────────
INSERT INTO encaissement (
    encaissement_tracking_id, numero_cheque,
    montant_cheque, montant_theorique,
    date_emission, date_encaissement,
    statut_cheque,
    organisme_emetteur_id, cheque_ordre_organisme_id,
    sinistre_id,
    active_data, deleted_data, from_table, created_at, created_by
)
SELECT
    'e2000000-0000-0000-0000-000000000002',
    'CHQ-D4-TEST-002',
    50000.00, 50000.00,
    '2024-01-26', '2024-02-02',
    'ENCAISSE',
    o.historique_id, o.historique_id,
    (SELECT historique_id FROM sinistre WHERE sinistre_tracking_id = 's0000000-0000-0000-0000-000000000001'),
    true, false, 'ENCAISSEMENT', NOW(), 'test-setup'
FROM organisme o
WHERE o.type_organisme = 'BUREAU_NATIONAL'
  AND o.active_data = true
  AND o.deleted_data = false
LIMIT 1;
