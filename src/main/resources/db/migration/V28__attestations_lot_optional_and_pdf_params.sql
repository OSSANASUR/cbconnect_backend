-- ── Lot d'approvisionnement : nom_fournisseur devient optionnel ──────────────
-- (la référence du lot reste UNIQUE et NOT NULL en DB, auto-générée côté service si vide)
ALTER TABLE lot_approvisionnement ALTER COLUMN nom_fournisseur DROP NOT NULL;

-- ── Paramètres pour la génération PDF des factures attestations ─────────────
-- L'index unique sur "cle" est partiel (WHERE active_data=true AND deleted_data=false),
-- donc incompatible avec ON CONFLICT → on utilise INSERT … WHERE NOT EXISTS pour l'idempotence.
INSERT INTO parametre (parametre_tracking_id, type_parametre, cle, valeur, description, created_at, created_by, active_data, deleted_data)
SELECT gen_random_uuid(), 'TEXTE', c.cle, c.valeur, c.description, NOW(), 'SYSTEM', true, false
  FROM (VALUES
    ('BNCB_RAISON_SOCIALE',      'BUREAU NATIONAL TOGOLAIS CARTE BRUNE CEDEAO', 'Raison sociale du Bureau (PDF facture)'),
    ('BNCB_VILLE',               'Lomé',                                        'Ville du Bureau (PDF facture)'),
    ('BNCB_PAYS',                'TOGO',                                        'Pays du Bureau (PDF facture)'),
    ('BNCB_SIGNATAIRE',          'Laurent SAVI',                                'Nom du signataire des factures (PDF)'),
    ('BNCB_BENEFICIAIRE_CHEQUE', 'BUREAU NATIONAL TOGOLAIS CARTE BRUNE CEDEAO', 'Bénéficiaire par défaut du chèque (proforma)')
  ) AS c(cle, valeur, description)
 WHERE NOT EXISTS (
    SELECT 1 FROM parametre p
     WHERE p.cle = c.cle
       AND p.active_data = true
       AND p.deleted_data = false
 );
