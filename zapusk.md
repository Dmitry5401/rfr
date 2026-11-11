# Запуск стенда Rangiffler

Для агента полный контекст проекта — skill `rfr` (`.claude/skills/rfr/SKILL.md`).
Ниже — краткая шпаргалка.

Требования: JDK 25, Node.js 22+, Docker Desktop (демон запущен).

```bash
docker compose up -d          # MySQL :3306, root/secret; дождись healthy
./gradlew runAll              # auth :9001, api :8081
cd rfr-gql-client && npm ci && npm run dev   # :3001
```

Остановка: `bash ports.sh`, `docker compose down`.
