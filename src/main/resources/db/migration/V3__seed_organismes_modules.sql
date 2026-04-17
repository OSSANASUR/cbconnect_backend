-- CBConnect V3 – Donnees initiales : BNCB Togo + Modules

-- ── Modules applicatifs CBConnect ─────────────────────────────────────────────
INSERT INTO module_entity (module_tracking_id, nom_module, description, actif, created_at, created_by, active_data, deleted_data)
VALUES
  (gen_random_uuid(), 'AUTH',          'Authentification et gestion des acces',             true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'REFERENTIELS',  'Referentiels CEDEAO (pays, compagnies)',             true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'SINISTRES',     'Gestion des sinistres transfrontaliers',             true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'PV',            'Enregistrement des PV de sinistres',                true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'EXPERTISES',    'Expertises medicales et materielles',               true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'RECLAMATIONS',  'Pieces de reclamation et factures victimes',        true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'INDEMNISATION', 'Calcul d offre et gestion ayants droit',            true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'FINANCE',       'Encaissements, paiements, prefinancement',          true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'ATTESTATIONS',  'Gestion des attestations Carte Brune',              true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'GED',           'Gestion Electronique des Documents (Paperless-ngx)', true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'COURRIERS',     'Courriers et rencontres bilaterales',               true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MESSAGERIE',    'Messagerie SMTP/IMAP integree',                     true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'BAREMES',       'Baremes CIMA (capitalisation, IP, PE, PM)',         true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'COMPTABILITE',  'Ecritures comptables automatiques',                 true, NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'DELAIS',        'Suivi des delais CEDEAO et penalites',              true, NOW(), 'SYSTEM', true, false)
ON CONFLICT DO NOTHING;

-- ── Organisme BNCB Togo ───────────────────────────────────────────────────────
INSERT INTO organisme (organisme_tracking_id, type_organisme, raison_sociale, code, email, responsable, code_pays, code_pays_bcb,
                       pays_id, active, created_at, created_by, active_data, deleted_data)
SELECT gen_random_uuid(), 'BUREAU_NATIONAL', 'Bureau National Carte Brune CEDEAO – Togo',
       'BNCB-TG', 'contact@bncb-togo.tg', 'Secretaire Executif',
       'TGO', 'TG', p.historique_id, true, NOW(), 'SYSTEM', true, false
FROM pays p WHERE p.code_iso = 'TGO' AND p.active_data = true
ON CONFLICT DO NOTHING;
