# Rangiffler — контекст для агента

Полные правила модулей — в `.claude/skills/` (`rfr`, `rfr-auth`, `rfr-api`, `rfr-gql-client`, `rfr-e2e`, `rfr-page-objects`). Краткая шпаргалка запуска — `zapusk.md`.

## Стенд

```bash
docker compose up -d --wait                    # MySQL, ждём healthy
./gradlew runAll                               # rfr-auth и rfr-api параллельно
(cd rfr-gql-client && npm ci && npm run dev)   # http://localhost:3001
./gradlew :rfr-e2e:installPlaywright           # браузер для тестов, один раз
./gradlew e2e                                  # всегда гоняет тесты (не UP-TO-DATE)
```

Остановка: Ctrl+C в терминалах `runAll` / `npm run dev`, при необходимости `bash ports.sh`, MySQL — `docker compose down` (volume `mysql-volume` сохраняется).
