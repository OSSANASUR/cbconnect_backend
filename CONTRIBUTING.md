# Contributing

## Migrations Flyway — convention d'équipe

Pour éviter les collisions de numéros entre branches, **toute nouvelle migration utilise une version horodatée** au format `VyyyyMMddHHmm__description.sql`.

```bash
# Créer une nouvelle migration
touch "src/main/resources/db/migration/V$(date +%Y%m%d%H%M)__ma_migration.sql"
```

Exemple : `V202604221430__ajout_colonne_statut.sql`

**Règles :**
- Ne pas renuméroter les migrations historiques (V1–V24) — elles restent telles quelles.
- Les migrations doivent être **idempotentes** quand c'est possible (`IF NOT EXISTS`, `WHERE NOT EXISTS`) — utile en cas de rejeu après incident.
- `spring.flyway.out-of-order=true` est activé : une migration horodatée plus tôt sera quand même appliquée si elle arrive après coup (merge tardif d'une branche).
- `spring.flyway.validate-migration-naming=true` : le build échoue si un fichier ne respecte pas le format `V<version>__<description>.sql`.

**En cas de conflit sur une branche** (Flyway refuse de démarrer avec "Found more than one migration with version X") : renommer la migration de la branche en version horodatée, pas de la branche principale.
