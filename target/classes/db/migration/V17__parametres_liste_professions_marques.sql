-- ============================================================
--  V17 — Paramètres de type LISTE : Professions + Marques
--
--  Convention clé : {CATEGORIE}.{CODE_MAJUSCULE}
--    ex: PROFESSION.MEDECIN   | MARQUE.TOYOTA
--
--  La catégorie sert de préfixe pour récupérer toute une liste :
--    SELECT * FROM parametre WHERE cle LIKE 'PROFESSION.%'
-- ============================================================

-- ── Professions ───────────────────────────────────────────────────

INSERT INTO parametre (parametre_tracking_id, type_parametre, cle, valeur, description,
                       created_at, created_by, active_data, deleted_data)
VALUES

-- Secteur public / administration
(gen_random_uuid(), 'LISTE', 'PROFESSION.AGENT_ETAT',            'Agent de l''État',              'Secteur public', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.MILITAIRE',             'Militaire',                     'Défense', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.POLICIER',              'Policier / Gendarme',           'Sécurité', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.MAGISTRAT',             'Magistrat',                     'Justice', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.DIPLOMATE',             'Diplomate',                     'Diplomatie', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.DOUANIER',              'Douanier',                      'Finances publiques', NOW(), 'SYSTEM', TRUE, FALSE),

-- Santé
(gen_random_uuid(), 'LISTE', 'PROFESSION.MEDECIN',               'Médecin',                       'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.PHARMACIEN',            'Pharmacien',                    'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.INFIRMIER',             'Infirmier / Infirmière',        'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.SAGE_FEMME',            'Sage-femme',                    'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.DENTISTE',              'Chirurgien-dentiste',           'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.BIOLOGISTE',            'Biologiste médical',            'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.KINESITHERAPEUTE',      'Kinésithérapeute',              'Santé', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.AIDE_SOIGNANT',        'Aide-soignant(e)',              'Santé', NOW(), 'SYSTEM', TRUE, FALSE),

-- Éducation
(gen_random_uuid(), 'LISTE', 'PROFESSION.ENSEIGNANT',            'Enseignant(e)',                 'Éducation', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.PROFESSEUR_UNIV',       'Professeur d''université',      'Éducation', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.DIRECTEUR_ECOLE',       'Directeur d''école',            'Éducation', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.ELEVE_ETUDIANT',        'Élève / Étudiant(e)',           'Éducation', NOW(), 'SYSTEM', TRUE, FALSE),

-- Commerce et transport
(gen_random_uuid(), 'LISTE', 'PROFESSION.COMMERCANT',            'Commerçant(e)',                 'Commerce', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.TRANSPORTEUR',          'Transporteur',                  'Transport', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.CHAUFFEUR',             'Chauffeur',                     'Transport', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.CONDUCTEUR_TAXI',       'Conducteur de taxi / moto',     'Transport', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.LOGISTICIEN',           'Agent logistique',              'Transport', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.IMPORTATEUR',           'Importateur / Exportateur',     'Commerce', NOW(), 'SYSTEM', TRUE, FALSE),

-- BTP / Artisanat
(gen_random_uuid(), 'LISTE', 'PROFESSION.INGENIEUR_BTP',         'Ingénieur BTP',                 'BTP', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.ARCHITECTE',            'Architecte',                    'BTP', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.MAÇON',                 'Maçon',                         'BTP', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.ELECTRICIEN',           'Électricien',                   'BTP', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.PLOMBIER',              'Plombier',                      'BTP', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.MENUISIER',             'Menuisier',                     'Artisanat', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.SOUDEUR',               'Soudeur / Métallurgiste',       'Artisanat', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.MECANICIEN',            'Mécanicien',                    'Artisanat', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.COUTURIER',             'Couturier / Tailleur',          'Artisanat', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.COIFFEUR',              'Coiffeur / Coiffeuse',          'Artisanat', NOW(), 'SYSTEM', TRUE, FALSE),

-- Agriculture et élevage
(gen_random_uuid(), 'LISTE', 'PROFESSION.AGRICULTEUR',           'Agriculteur',                   'Agriculture', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.ELEVEUR',               'Éleveur',                       'Agriculture', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.PECHEUR',               'Pêcheur',                       'Agriculture', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.AGRONOME',              'Agronome / Ingénieur agri.',    'Agriculture', NOW(), 'SYSTEM', TRUE, FALSE),

-- Professions libérales
(gen_random_uuid(), 'LISTE', 'PROFESSION.AVOCAT',                'Avocat',                        'Professions libérales', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.NOTAIRE',               'Notaire / Huissier',            'Professions libérales', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.COMPTABLE',             'Expert-comptable',              'Professions libérales', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.AUDITEUR',              'Auditeur / Consultant',         'Professions libérales', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.JOURNALISTE',           'Journaliste',                   'Médias', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.INFORMATICIEN',         'Informaticien / Développeur',   'Tech', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.INGENIEUR',             'Ingénieur (autre)',             'Industrie', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.GESTIONNAIRE',          'Gestionnaire / Manager',        'Gestion', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.BANQUIER',              'Banquier / Financier',          'Finance', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.ASSUREUR',              'Agent / Courtier d''assurance', 'Finance', NOW(), 'SYSTEM', TRUE, FALSE),

-- Secteur informel / autres
(gen_random_uuid(), 'LISTE', 'PROFESSION.MENAGER',               'Ménager(ère) / Femme au foyer', 'Autre', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.RETRAITE',              'Retraité(e)',                   'Autre', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.SANS_EMPLOI',           'Sans emploi',                   'Autre', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'PROFESSION.AUTRE',                 'Autre profession',              'Autre', NOW(), 'SYSTEM', TRUE, FALSE),

-- ── Marques de véhicules ─────────────────────────────────────────

-- Japonaises (très répandues en Afrique de l'Ouest)
(gen_random_uuid(), 'LISTE', 'MARQUE.TOYOTA',       'Toyota',       'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.NISSAN',        'Nissan',       'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.HONDA',         'Honda',        'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.MITSUBISHI',    'Mitsubishi',   'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.MAZDA',         'Mazda',        'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.SUZUKI',        'Suzuki',       'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.ISUZU',         'Isuzu',        'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.SUBARU',        'Subaru',       'Japonais', NOW(), 'SYSTEM', TRUE, FALSE),

-- Françaises
(gen_random_uuid(), 'LISTE', 'MARQUE.PEUGEOT',       'Peugeot',      'Français', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.RENAULT',        'Renault',      'Français', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.CITROEN',        'Citroën',      'Français', NOW(), 'SYSTEM', TRUE, FALSE),

-- Allemandes
(gen_random_uuid(), 'LISTE', 'MARQUE.MERCEDES',       'Mercedes-Benz','Allemand', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.BMW',            'BMW',          'Allemand', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.VOLKSWAGEN',     'Volkswagen',   'Allemand', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.AUDI',           'Audi',         'Allemand', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.OPEL',           'Opel',         'Allemand', NOW(), 'SYSTEM', TRUE, FALSE),

-- Coréennes
(gen_random_uuid(), 'LISTE', 'MARQUE.HYUNDAI',        'Hyundai',      'Coréen', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.KIA',            'Kia',          'Coréen', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.SSANGYONG',      'SsangYong',    'Coréen', NOW(), 'SYSTEM', TRUE, FALSE),

-- Américaines
(gen_random_uuid(), 'LISTE', 'MARQUE.FORD',           'Ford',         'Américain', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.CHEVROLET',      'Chevrolet',    'Américain', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.JEEP',           'Jeep',         'Américain', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.DODGE',          'Dodge',        'Américain', NOW(), 'SYSTEM', TRUE, FALSE),

-- Chinoises (montée en puissance Afrique)
(gen_random_uuid(), 'LISTE', 'MARQUE.GREAT_WALL',     'Great Wall',   'Chinois', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.CHERY',          'Chery',        'Chinois', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.GEELY',          'Geely',        'Chinois', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.BYD',            'BYD',          'Chinois', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.HAVAL',          'Haval',        'Chinois', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.DONGFENG',       'Dongfeng',     'Chinois', NOW(), 'SYSTEM', TRUE, FALSE),

-- Utilitaires / camions courants
(gen_random_uuid(), 'LISTE', 'MARQUE.DACIA',          'Dacia',        'Roumain', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.LAND_ROVER',     'Land Rover',   'Britannique', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.VOLVO',          'Volvo',        'Suédois', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.MAN',            'MAN',          'Camion', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.SCANIA',         'Scania',       'Camion', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.DAF',            'DAF',          'Camion', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.IVECO',          'Iveco',        'Camion', NOW(), 'SYSTEM', TRUE, FALSE),
(gen_random_uuid(), 'LISTE', 'MARQUE.AUTRE',          'Autre marque', 'Autre',  NOW(), 'SYSTEM', TRUE, FALSE)

ON CONFLICT (cle) WHERE active_data = true AND deleted_data = false DO NOTHING;
