---
name: rfr-gql-client
description: Создаёт и меняет код React-фронтенда Rangiffler в rfr-gql-client (Vite, Apollo Client, MUI, OAuth PKCE, страницы и хуки GraphQL). Применять к api/, components/, hooks/, pages/, context/, types/. Spring-бэкенд и Java E2E / Page Objects — вне ответственности этого skill.
---

# Модуль rfr-gql-client

SPA Rangiffler: React 19, TypeScript 6, Vite 8, Apollo Client 4, MUI 9, React Router 8. Dev-сервер — порт `3001` (`VITE_FRONT_HOST` / `vite.config.ts`). Собирается отдельно от Gradle: `npm ci` / `npm run dev`.

## Контракт

1. Конфиг только из `.env` (`VITE_AUTH_URL`, `VITE_API_URL`, `VITE_FRONT_URL`, `VITE_CLIENT_ID=client`, …). Порт фронта должен совпадать с redirect URI клиента в `rfr-auth`.
2. OAuth Authorization Code + PKCE: Login/`/redirect` → generate `code_verifier`/`code_challenge` (S256) в localStorage → `/oauth2/authorize`; `/authorized` меняет `code` на токены через `authClient.getToken`. В Apollo уходит **только** `access_token` (`Authorization: Bearer …`). `id_token` — лишь для `id_token_hint` при logout.
3. HTTP 401 от `rfr-api`: `ErrorLink` очищает сессию и уводит на `/`. Не подставляй токены в обход этого потока.
4. Logout (`MenuAppBar`): `revokeToken` → очистка Apollo store → `/connect/logout?id_token_hint=…&post_logout_redirect_uri=…/logout` → `/logout` чистит localStorage.
5. Защищённые маршруты — `PrivateRoute`: `GetUser` (создаёт API-профиль при первом входе); `UNAUTHORIZED`/`FORBIDDEN` → `/`; прочие ошибки → `QueryErrorAlert` + Retry.
6. Каждая GraphQL-операция — отдельный хук в `src/hooks` с явным `operationName` (`GetUser`, `GetFeed`, `UpdateUser`, `FriendshipAction`, …). Новую операцию не размазывай по компонентам — добавь хук. Имена операций важны для перехвата в E2E.
7. Ошибки с `extensions.classification` `BAD_REQUEST` / `NOT_FOUND` — текст с сервера (`graphqlError.ts`); остальные — общее сообщение. Snackbar / обновлённая карточка после mock-мутации фото не доказывают сохранение на бэкенде.
8. Кэш Apollo: сущности по `id`, `Feed` с `merge: true`. После people/friendship-мутаций перечитывай связанные people-запросы так же, как существующие хуки.
9. Маршруты: `/` landing; `/redirect`, `/authorized`, `/logout` — auth-flow; `/my-travels`, `/people`, `/profile` — private. Новые экраны клади в `pages/`, общие части — в `components/`.
10. Тема: `mui-mode` в localStorage (`light`/`dark`/`system`); в ссылки на auth передавай `theme`. Карта: `rangiffler-map-collapsed`.
11. Лента/фото/лайки на API пока mock — UI может показывать успех, но после refetch данные вернутся к JSON. Не «чини» это на фронте записью в localStorage.
12. Без лишних комментариев. Структура и таблицы хуков — `rfr-gql-client/README.md`.

## Пакеты / каталоги

| Путь | Что лежит |
|---|---|
| `src/api` | `apolloClient`, `authClient`, `authUtils` (PKCE, ссылки, localStorage), `graphqlError` |
| `src/hooks` | По хуку на GraphQL-операцию |
| `src/pages` | Landing, Redirect, Authorized, Logout, MyTravels, People, Profile |
| `src/components` | AppContent, PrivateRoute, MenuAppBar, карта, лента, люди, профиль, модалки |
| `src/context` | Session, Countries, Dialog (форма фото), SnackBar + `use*` |
| `src/types` | Connection, Country, Photo, User, Likes, Stat, Order |
| `src/theme.tsx` | MUI light/dark |

## localStorage (не ломай ключи)

`access_token`, `id_token`, `codeVerifier`, `codeChallenge`, `mui-mode`, `rangiffler-map-collapsed`.

## Проверка результата

```bash
cd rfr-gql-client
npm ci
npm run lint
npm run build
npm run dev   # http://localhost:3001 — нужны rfr-auth :9001 и rfr-api :8081
# бэкенды: из корня репозитория ./gradlew runAll
```

`lint` (`--max-warnings 0`) и `build` (`tsc && vite build`) — обязательный минимум после правок TypeScript/React. Сценарий в браузере не заменяет красный `tsc`/eslint.

## Контекст проекта

| Модуль | Роль | Порт |
|---|---|---|
| `rfr-auth` | Authorization Server | 9001 |
| `rfr-api` | GraphQL API | 8081 |
| `rfr-gql-client` | React + Apollo | 3001 |
| `rfr-e2e` | Playwright / JUnit E2E | — |

Бэкенды вместе: `./gradlew runAll`. Page Objects и локаторы UI для тестов пишет skill `rfr-page-objects` в `rfr-e2e`, не этот skill.
