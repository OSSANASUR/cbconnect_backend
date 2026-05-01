-- =============================================================================
-- V202604301002 — Habilitation PARAM_MOTIFS_GERER
--   Gérer les motifs paramétrés (module ADMINISTRATION — paramètres métier)
-- =============================================================================

-- 1. Création de l'habilitation (idempotent)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT 'PARAM_MOTIFS_GERER',
       'Gérer les motifs paramétrés',
       'Créer, modifier et supprimer les motifs de règlement, annulation et préfinancement',
       'UPDATE',
       'GLOBAL',
       (SELECT historique_id FROM module_entity
          WHERE nom_module = 'ADMINISTRATION' AND active_data = true AND deleted_data = false),
       true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h
  WHERE h.code_habilitation = 'PARAM_MOTIFS_GERER'
    AND h.active_data = true AND h.deleted_data = false
);

-- 2. Attribution de l'habilitation au profil ADMIN (idempotent)
INSERT INTO profil_habilitations (profil_id, habilitation_id)
SELECT p.historique_id, h.historique_id
FROM profil p
JOIN habilitation h ON h.code_habilitation = 'PARAM_MOTIFS_GERER'
                   AND h.active_data = true AND h.deleted_data = false
WHERE upper(p.profil_nom) = 'ADMIN'
  AND p.active_data = true AND p.deleted_data = false
  AND NOT EXISTS (
    SELECT 1 FROM profil_habilitations ph
    WHERE ph.profil_id = p.historique_id AND ph.habilitation_id = h.historique_id
  );
