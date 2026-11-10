---
name: rfr-auth
description: Создаёт и меняет код модуля rfr-auth — Spring Authorization Server Rangiffler (OAuth 2.1 / OIDC, login/register, токены, схема rangiffler-auth). Применять к SecurityFilterChain, контроллерам login/register, UserDetails, JWT customizer, revocation публичного клиента, Flyway и Thymeleaf. GraphQL API, фронт и E2E — вне ответственности этого skill.
---

# Модуль rfr-auth

Authorization Server Rangiffler: Java 25, Spring Boot 4.1.1, Spring Security 7 + Spring Authorization Server, JPA, Flyway, MySQL (`rangiffler-auth`), Thymeleaf. Порт `9001`. Корневой пакет `io.student.rangiffler`.

## Контракт

1. Публичный SPA-клиент `client`: `client-authentication-methods: none`, grant `authorization_code`, PKCE обязателен, redirect `http://localhost:3001/authorized`, post-logout `http://localhost:3001/logout`, scopes `openid`/`profile`. Секрета клиента нет; refresh token не выдаётся.
2. Два `SecurityFilterChain` в `RangifflerAuthServiceConfig`: Order 1 — OAuth/OIDC endpoints; Order 2 — приложение (`/login`, `/register`, статика). Неаутентифицированный HTML на authorize уходит на `/login`.
3. Access token (JWT) для `rfr-api`: `sub` = username, `aud` = `rangiffler-api` (`rangiffler-api.audience`), `authorities` из таблицы `authority` без `FACTOR_*`. Кастомизация только в `AccessTokenCustomizer`. `id_token` — для фронта и `id_token_hint` при logout, не для Bearer в API.
4. JWKS (`jwkSource`) генерируется при старте: после рестарта `rfr-auth` все ранее выданные токены невалидны. OAuth-авторизации in-memory — в БД не класть.
5. `/oauth2/revoke` для публичного клиента: только через `PublicClientRevocationAuthenticationConverter` + `PublicClientRevocationAuthenticationProvider` (достаточно `client_id`). Не ломай стандартную клиентскую аутентификацию на остальных endpoints.
6. Регистрация: `GET/POST /register`, CSRF той же сессии, поля `username`/`password`/`passwordSubmit`. Валидация: длина, `@NoWhitespace` (`Character.isWhitespace`), `@equalPasswords`. Успех — 201; занятый username или ошибка формы — 400. Authorities нового пользователя — `read` и `write`. Пароль — `DelegatingPasswordEncoder` (`{bcrypt}`).
7. Регистрация пишет только в `rangiffler-auth`. API-профиль в `rangiffler-api` создаётся при первом авторизованном `Query.user` — не дублируй его здесь.
8. `GET /login` без сохранённого OAuth-запроса (`/oauth2/authorize` с redirect фронта) уводит на фронт — это ожидаемо. Вход начинать с authorize или кнопки Login.
9. Logout: revoke access token → `/connect/logout` с `id_token_hint` → `OidcClearCookiesLogoutHandler` чистит `JSESSIONID` и `XSRF-TOKEN`.
10. Тема: `ThemeCookieFilter` кладёт `?theme=light|dark` в cookie `rangiffler-theme`. CORS origins — `CorsCustomizer` под `rangiffler-front.base-uri`.
11. Без комментариев и Javadoc: смысл передают имена классов и методов. Подробности — `rfr-auth/README.md`.

## Пакеты

| Пакет / путь | Что лежит |
|---|---|
| `config` | SecurityFilterChain, JWKS, PasswordEncoder, AccessTokenCustomizer, revocation для public client, ThemeCookieFilter |
| `controller` | Login, Register, Error |
| `data` + `repository` | `UserEntity`, `AuthorityEntity`, `Authority`, `UserRepository` |
| `model` | `RegistrationForm` |
| `service` | `UserService` / impl, `DatabaseUserDetailsService`, logout handler, CORS, GlobalExceptionHandler |
| `validation` | `@equalPasswords`, `@NoWhitespace` |
| `resources/templates` | login / register / error |
| `resources/db/migration/rangiffler-auth` | Flyway: `user`, `authority` |

## Endpoints (ориентиры)

`/oauth2/authorize`, `/oauth2/token`, `/oauth2/revoke`, `/oauth2/jwks`, `/.well-known/openid-configuration`, `/userinfo`, `/connect/logout`, `/login`, `/register`.

## Проверка результата

```bash

# оба бэкенда одной командой (нужен org.gradle.parallel=true):
./gradlew runAll
curl -s http://localhost:9001/.well-known/openid-configuration
```

Прогон сценария login/register через браузер или E2E не заменяет проверку, что сервис стартует и отдаёт OIDC discovery. Изменения revocation/token проверяй запросом к `/oauth2/token` или `/oauth2/revoke` с публичным `client_id`.

## Контекст проекта

| Модуль | Роль | Порт |
|---|---|---|
| `rfr-auth` | Authorization Server, схема `rangiffler-auth` | 9001 |
| `rfr-api` | GraphQL Resource Server, схема `rangiffler-api` | 8081 |
| `rfr-gql-client` | React + Apollo (`npm run dev`) | 3001 |
| `rfr-e2e` | JUnit 6 + Playwright Java + Allure 3 | — |

MySQL: `root` / `secret`, порт 3306. Стенд: JDK 25, Node 22+, Docker. Бэкенды вместе: `./gradlew runAll`.
