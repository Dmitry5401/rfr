# Запуск стенда Rangiffler

Для агента полный контекст проекта — skill `rfr` (`.claude/skills/rfr/SKILL.md`).
Ниже — краткая шпаргалка.

Требования: JDK 25, Node.js 22+, Docker Desktop (демон запущен).

```bash
docker compose up -d          # MySQL :3306, root/secret; дождись healthy
./gradlew runAll              # auth :9001, api :8081
cd rfr-gql-client && npm ci && npm run dev   # :3001
./gradlew e2e                 # все тесты rfr-e2e
```

## Остановка

В терминале, где крутится процесс:

- `./gradlew runAll` → **Ctrl+C** (auth :9001 и api :8081)
- `npm run dev` → **Ctrl+C** (фронт :3001)

Если порты остались заняты (из корня репо):

```bash
bash ports.sh                 # kill процессов на 9001, 8081, 3001
```

MySQL:

```bash
docker compose down           # контейнер rfr-mysql; volume mysql-volume сохраняется
```
