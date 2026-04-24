-- V27__organisme_adresse_et_parametres_mail.sql
-- Adresse postale sur Organisme (nullable, retro-compatible) + parametres pour
-- coordonnees du BNCB emetteur affichees dans le pied de page des mails.

ALTER TABLE organisme
    ADD COLUMN adresse        VARCHAR(255),
    ADD COLUMN boite_postale  VARCHAR(50),
    ADD COLUMN ville          VARCHAR(100);

INSERT INTO parametre (parametre_tracking_id, type_parametre, cle, valeur, description, created_at, created_by, active_data, deleted_data) VALUES
  (gen_random_uuid(), 'MAIL',   'MAIL_FOOTER_ORGANISME',         'Carte Brune CEDEAO — Bureau National Togo',                                'Nom affiche dans le pied de page des mails',                NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_FOOTER_ADRESSE',           '128, Boulevard du 13 Janvier · Immeuble BIDC, 4ᵉ étage',                   'Adresse physique du BNCB emetteur',                          NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_FOOTER_BP',                '01 BP 2258 · Lomé, Togo',                                                  'Boite postale du BNCB emetteur',                             NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_FOOTER_TEL',               '(+228) 22 22 39 55',                                                       'Telephone du BNCB emetteur',                                 NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_FOOTER_EMAIL',             'contact@cartebrune.org',                                                   'Email contact public du BNCB emetteur',                      NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_FOOTER_LOGO_URL',          'https://cartebrune.org/squelettes/imgs/logo-fr.png',                       'URL absolue du logo affiche dans les mails',                 NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_SUPPORT_EMAIL',            'support@bncb-togo.com',                                                    'Email de support affiche dans les mails et la page d''erreur',NOW(),'SYSTEM', true, false),
  (gen_random_uuid(), 'MAIL',   'MAIL_FRONTEND_BASE_URL',        'http://localhost:3000',                                                    'Base URL du front pour la construction des liens mail',      NOW(), 'SYSTEM', true, false),
  (gen_random_uuid(), 'SYSTEM', 'ACCOUNT_SETUP_TOKEN_TTL_DAYS',  '7',                                                                        'Duree de validite du lien d''activation (jours)',             NOW(), 'SYSTEM', true, false)
ON CONFLICT DO NOTHING;
