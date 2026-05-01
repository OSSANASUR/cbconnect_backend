-- V202604301013__fix_victimes_seed_statut.sql
-- Correctif : la migration V202604301012 a inséré les 3 victimes avec
-- statut_victime = 'EN_COURS_INDEMNISATION' qui N'EXISTE PAS dans l'enum Java
-- StatutVictime. Valeurs réelles : NEUTRE, ATTENTE_PIECES, OFFRE_EFFECTUE,
-- ACCORD, REJET, BAP, SOLDE. On corrige vers ATTENTE_PIECES (le plus proche
-- sémantiquement de "en cours d'indemnisation").

UPDATE victime
SET statut_victime = 'ATTENTE_PIECES'
WHERE statut_victime = 'EN_COURS_INDEMNISATION';
