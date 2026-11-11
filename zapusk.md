# Запуск стенда Rangiffler

Требования: JDK 25, Node.js 22+, Docker.

## 1. MySQL

Из корня репозитория:

```bash
docker compose up -d
# или то же самое:
bash localenv.sh
```

- контейнер `rfr-mysql`, образ `mysql:8.4.7`
- порт `3306`, пользователь `root`, пароль `secret`
- дождись `healthy`: `docker compose ps`

Не используй старый вариант `localenv.sh`, который останавливал все Docker-контейнеры машины: сейчас скрипт только поднимает MySQL через compose и ждёт healthcheck.

## 2. Бэкенды

Одной командой (`org.gradle.parallel=true` уже в `gradle.properties`):

```bash
./gradlew runAll
```

| Сервис | URL |
|---|---|
| `rfr-auth` | http://localhost:9001 |
| `rfr-api` | http://localhost:8081/graphql (GraphiQL: `/graphiql`) |

По отдельности: `./gradlew :rfr-auth:bootRun` и `./gradlew :rfr-api:bootRun`.

Проверка auth:

```bash
curl -s http://localhost:9001/.well-known/openid-configuration
```

## 3. Фронт

```bash
cd rfr-gql-client
npm ci
npm run dev
```

Откроется http://localhost:3001/

## Остановка

```bash
bash ports.sh          # освобождает 9001, 8081, 3001
docker compose down    # MySQL; данные в volume mysql-volume сохраняются
```
