---
name: rfr-e2e
description: Создаёт и меняет код модуля rfr-e2e, кроме Page Objects. Применять к тестам, фикстурам, TestConfig, JUnit-расширениям и SQL ресурсов. Страницы и компоненты UI — skill rfr-page-objects.
---

# Модуль rfr-e2e

E2E-тесты Rangiffler: JUnit 6, Playwright Java (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур. Один source set `src/test`, корневой пакет `io.student.rangiffler`.

Page Objects и компоненты этот skill не пишет. Для `ui/page` и `ui/component` применяй skill `rfr-page-objects`.

## Пакеты

Новые классы кладутся в существующие пакеты. Описание — `rfr-e2e/README.md`, раздел «Архитектура».

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `jupiter` | JUnit: `annotation` (`@User`), `extension` (`UserExtension`), позже `condition`, `probe` |
| `data` | тестовые данные и их владельцы: `TestUser`, `UserFixture`; `db` — `Databases`, `Jdbc` |
| `utils` | статические утилиты без состояния: `RandomDataUtils` |
| `ui` | только страницы и компоненты, по skill `rfr-page-objects` |
| `test` | только тесты, по фичам: `auth`, `profile`, позже `people` |
| `assertions` | проверки, которые не являются методами страницы |

Границы: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL. `ThreadLocal` для состояния теста не используется: состояние живёт в Store расширения. Тестовые данные создаются и удаляются фикстурами (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE` и `TRUNCATE`.

## Настройки тестов

`TestConfig` читает `-Drfr.<ключ>=…` → переменная `RFR_<КЛЮЧ>` → значение по умолчанию. Gradle передаёт все `-Drfr.*` в тестовую JVM. Адреса и учётка БД не хардкодятся в тестах и фикстурах.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схему добавляет код) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` | `chromium` / `true` |

## Прогон

```bash
./gradlew :rfr-e2e:installPlaywright
./gradlew :rfr-e2e:test --tests '*RegistrationTest' --rerun-tasks
./gradlew :rfr-e2e:test --tests '*MainPageTest' --rerun-tasks
./gradlew :rfr-e2e:allureReport