-- Table et seed du bareme pretium doloris / prejudice esthetique (art. 262 CIMA)
-- Points = % du SMIG annuel applique pour chaque qualification.
-- Source : BNCB historique (9 lignes fixes de l'enum QualificationPretium).

CREATE TABLE IF NOT EXISTS bareme_pretium_doloris (
    id              SERIAL PRIMARY KEY,
    qualification   VARCHAR(50) NOT NULL UNIQUE,
    points          INTEGER NOT NULL CHECK (points >= 0),
    moral           BOOLEAN NOT NULL DEFAULT false,
    actif           BOOLEAN NOT NULL DEFAULT true
);

COMMENT ON TABLE bareme_pretium_doloris IS 'Barème pretium doloris / préjudice esthétique (art. 262 CIMA). Points = % SMIG annuel.';

INSERT INTO bareme_pretium_doloris (qualification, points, moral, actif)
VALUES
  ('NEANT',           0,   false, true),
  ('TRES_LEGER',      5,   false, true),
  ('LEGER',           10,  false, true),
  ('MODERE',          20,  false, true),
  ('MOYEN',           40,  false, true),
  ('ASSEZ_IMPORTANT', 60,  false, true),
  ('IMPORTANT',       100, false, true),
  ('TRES_IMPORTANT',  150, false, true),
  ('EXCEPTIONNEL',    300, false, true)
ON CONFLICT (qualification) DO NOTHING;
