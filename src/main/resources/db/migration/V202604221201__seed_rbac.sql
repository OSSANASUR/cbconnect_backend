-- V20 : Seed des modules et habilitations RBAC par defaut

-- 1. Modules metier (uniquement ceux absents) --------------------------------
INSERT INTO module_entity (nom_module, description, actif, active_data, deleted_data,
  created_at, created_by, from_table)
SELECT nom_module, description, actif, active_data, deleted_data, created_at, created_by, from_table
FROM (VALUES
  ('STATISTIQUES',   'Rapports et tableaux de bord', true, true, false, now(), 'SYSTEM_SEED', 'MODULE_ENTITY'),
  ('ADMINISTRATION', 'Administration plateforme',     true, true, false, now(), 'SYSTEM_SEED', 'MODULE_ENTITY')
) AS v(nom_module, description, actif, active_data, deleted_data, created_at, created_by, from_table)
WHERE NOT EXISTS (
  SELECT 1 FROM module_entity m WHERE m.nom_module = v.nom_module AND m.active_data = true AND m.deleted_data = false
);

-- 2. Habilitations (idempotent : on ignore si code_habilitation existe deja) -

-- SINISTRES (6)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='SINISTRES' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('SINISTRES_CREATE',   'Creer un sinistre',       'Creer un nouveau dossier sinistre', 'CREATE',   'ORGANISME'),
  ('SINISTRES_READ',     'Consulter les sinistres',  'Lister et consulter les sinistres', 'READ',     'ORGANISME'),
  ('SINISTRES_UPDATE',   'Modifier un sinistre',     'Modifier un dossier sinistre',      'UPDATE',   'ORGANISME'),
  ('SINISTRES_DELETE',   'Supprimer un sinistre',    'Suppression logique',               'DELETE',   'ORGANISME'),
  ('SINISTRES_VALIDATE', 'Valider un sinistre',      'Validation officielle',             'VALIDATE', 'ORGANISME'),
  ('SINISTRES_EXPORT',   'Exporter les sinistres',   'Export Excel/CSV',                  'EXPORT',   'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- FINANCE (5)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='FINANCE' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('FINANCE_CREATE',   'Saisir encaissement/paiement', 'Creer une operation',  'CREATE',   'ORGANISME'),
  ('FINANCE_READ',     'Consulter la finance',          'Lister operations',    'READ',     'ORGANISME'),
  ('FINANCE_UPDATE',   'Modifier une operation',        'Correction operation', 'UPDATE',   'ORGANISME'),
  ('FINANCE_VALIDATE', 'Valider une operation',         'Validation comptable', 'VALIDATE', 'ORGANISME'),
  ('FINANCE_EXPORT',   'Exporter etats financiers',     'Export Excel/PDF',     'EXPORT',   'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- COURRIERS (4)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='COURRIERS' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('COURRIERS_CREATE', 'Creer un courrier',       'Enregistrer un courrier', 'CREATE', 'ORGANISME'),
  ('COURRIERS_READ',   'Consulter les courriers', 'Lister les courriers',    'READ',   'ORGANISME'),
  ('COURRIERS_UPDATE', 'Modifier un courrier',    'Modifier le registre',    'UPDATE', 'ORGANISME'),
  ('COURRIERS_DELETE', 'Supprimer un courrier',   'Suppression logique',     'DELETE', 'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- GED (3)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='GED' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('GED_CREATE', 'Uploader un document',    'Ajout document GED',  'CREATE', 'ORGANISME'),
  ('GED_READ',   'Consulter les documents', 'Lister/telecharger',  'READ',   'ORGANISME'),
  ('GED_DELETE', 'Supprimer un document',   'Suppression logique', 'DELETE', 'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- INDEMNISATION (2)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='INDEMNISATION' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('INDEMNISATION_READ',     'Consulter les calculs',     'Voir le bareme CIMA', 'READ',     'ORGANISME'),
  ('INDEMNISATION_VALIDATE', 'Valider une indemnisation', 'Validation montant',  'VALIDATE', 'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- ATTESTATIONS (3)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='ATTESTATIONS' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('ATTESTATIONS_CREATE', 'Commander des attestations', 'Passer commande', 'CREATE', 'ORGANISME'),
  ('ATTESTATIONS_READ',   'Consulter les commandes',    'Liste commandes', 'READ',   'ORGANISME'),
  ('ATTESTATIONS_EXPORT', 'Facturation PDF',            'Export facture',  'EXPORT', 'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- STATISTIQUES (2)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='STATISTIQUES' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('STATISTIQUES_READ',   'Consulter les statistiques', 'Acces aux etats',  'READ',   'ORGANISME'),
  ('STATISTIQUES_EXPORT', 'Exporter les statistiques',  'Export PDF/Excel', 'EXPORT', 'ORGANISME')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);

-- ADMINISTRATION (5)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT code_hab, libelle, descr, action, type_acces,
  (SELECT historique_id FROM module_entity WHERE nom_module='ADMINISTRATION' AND active_data=true AND deleted_data=false),
  true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
FROM (VALUES
  ('ADMIN_UTILISATEURS_MANAGE',  'Gerer les utilisateurs',  'CRUD utilisateurs',      'UPDATE', 'GLOBAL'),
  ('ADMIN_PROFILS_MANAGE',       'Gerer les profils',       'CRUD profils RBAC',      'UPDATE', 'GLOBAL'),
  ('ADMIN_HABILITATIONS_MANAGE', 'Gerer les habilitations', 'CRUD habilitations',     'UPDATE', 'GLOBAL'),
  ('ADMIN_ORGANISMES_MANAGE',    'Gerer les organismes',    'CRUD organismes',        'UPDATE', 'GLOBAL'),
  ('ADMIN_PARAMETRES_MANAGE',    'Gerer les parametres',    'CRUD parametres metier', 'UPDATE', 'GLOBAL')
) AS v(code_hab, libelle, descr, action, type_acces)
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h WHERE h.code_habilitation = v.code_hab AND h.active_data = true AND h.deleted_data = false
);
