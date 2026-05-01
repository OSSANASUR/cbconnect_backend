-- V23__habilitation_2fa_organisme.sql
-- Ajoute l'habilitation dediee au toggle 2FA d'un organisme + l'attribue au profil ADMIN.

-- 1. Creation de l'habilitation ORGANISMES_2FA_MANAGE (module ADMINISTRATION)
INSERT INTO habilitation (code_habilitation, libelle_habilitation, description, action,
  type_acces, module_entity_id, active_data, deleted_data, created_at, created_by, from_table)
SELECT 'ORGANISMES_2FA_MANAGE',
       'Gerer la double authentification d''un organisme',
       'Activer ou desactiver la 2FA au niveau de l''organisme',
       'UPDATE',
       'GLOBAL',
       (SELECT historique_id FROM module_entity
          WHERE nom_module='ADMINISTRATION' AND active_data=true AND deleted_data=false),
       true, false, now(), 'SYSTEM_SEED', 'HABILITATION'
WHERE NOT EXISTS (
  SELECT 1 FROM habilitation h
  WHERE h.code_habilitation = 'ORGANISMES_2FA_MANAGE'
    AND h.active_data = true AND h.deleted_data = false
);

-- 2. Attribution de l'habilitation au profil ADMIN (idempotent)
INSERT INTO profil_habilitations (profil_id, habilitation_id)
SELECT p.historique_id, h.historique_id
FROM profil p
JOIN habilitation h ON h.code_habilitation = 'ORGANISMES_2FA_MANAGE'
                   AND h.active_data = true AND h.deleted_data = false
WHERE upper(p.profil_nom) = 'ADMIN'
  AND p.active_data = true AND p.deleted_data = false
  AND NOT EXISTS (
    SELECT 1 FROM profil_habilitations ph
    WHERE ph.profil_id = p.historique_id AND ph.habilitation_id = h.historique_id
  );
