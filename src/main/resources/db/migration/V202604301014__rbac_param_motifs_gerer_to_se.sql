-- =============================================================================
-- V202604301014 — Étend l'habilitation PARAM_MOTIFS_GERER au profil SE
--   Permet la création inline de motifs depuis les formulaires de règlement
--   et préfinancement (côté frontend, composant ParamMotifSelect).
-- =============================================================================

INSERT INTO profil_habilitations (profil_id, habilitation_id)
SELECT p.historique_id, h.historique_id
FROM profil p
JOIN habilitation h ON h.code_habilitation = 'PARAM_MOTIFS_GERER'
                   AND h.active_data = true AND h.deleted_data = false
WHERE upper(p.profil_nom) IN ('SE', 'SECRETAIRE_EXECUTIF')
  AND p.active_data = true AND p.deleted_data = false
  AND NOT EXISTS (
    SELECT 1 FROM profil_habilitations ph
    WHERE ph.profil_id = p.historique_id AND ph.habilitation_id = h.historique_id
  );
