# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузер Chromium
./gradlew e2e                                             # все UI-тесты (тег ui)
./gradlew :rfr-e2e:e2e --tests '*MainPageTest'            # один тест
./gradlew :rfr-e2e:test                                   # всё без тега ui, стенд не нужен; с --tests тег ui не отсекается (запуск из IDE)
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

Стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README).

Тесты, которым нужен браузер и стенд, помечены `@Tag("ui")` и запускаются задачей `e2e`. Задача `test` исключает тег `ui` и обходится без стенда. Обе задачи запускают тесты при каждом вызове, `--rerun-tasks` не нужен. Перед первым классом с тегом `ui` `StandReadinessExtension` ждёт MySQL, `rfr-auth`, `rfr-api` и фронт не дольше `rfr.standTimeout` секунд, поэтому стенд ждёт любой запуск UI-теста, в том числе из IDE; затем, только в задаче `e2e`, `StandSchemaExtension` сверяет миграции. Задача `waitForStand` (`scripts/wait-for-stand.sh`) ждёт стенд отдельно от тестов. После прогона `AllureEnvironmentExtension` пишет `environment.properties` в каталог результатов Allure.

## Конфигурация

`TestConfig` читает `-Drfr.<ключ>` → `RFR_<КЛЮЧ>` → значение по умолчанию. Gradle передаёт все `-Drfr.*` в тестовую JVM.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схема добавляется кодом) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` | `chromium` / `true` |
| `rfr.standCheck` | `true` — ждать стенд перед UI-тестами |
| `rfr.migrationCheck` | `false`, задача `e2e` включает |
| `rfr.standTimeout` | `180` (секунды) |
| `rfr.commit` | `unknown`, Gradle передаёт короткий хеш `HEAD` |

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | только `TestConfig` — единственная точка чтения настроек |
| `stand` | внешний стенд как объект: `Stand` ждёт, пока ответят все `Service` стенда (`TcpService`, `HttpService`); `Migrations` сверяет схему БД с миграциями бэкенда через Flyway validate и отдаёт текущую версию схемы |
| `jupiter` | всё про JUnit: `annotation` (`@User`, `@ProbeTarget`), `extension` (`UserExtension`; через ServiceLoader в этом порядке: `AllureEnvironmentExtension` — после прогона пишет окружение для Allure, `StandReadinessExtension` — перед задачей `e2e` ждёт стенд, `StandSchemaExtension` — затем сверяет миграции), `condition` (`ProbeTargetCondition` — мишени probe-тестов выполняются только внутри EngineTestKit), `probe` |
| `data` | тестовые данные и их владельцы: `TestUser`, `UserFixture` с реализацией `SingleUserFixture` и декораторами `UserWithFriendsFixture`, `UserWithIncomeInvitationsFixture`, `UserRequest` собирает их цепочку; `db` — `Databases`, `Jdbc` и неизменяемые объекты строк (`Row`/`Rows`: `AuthAccount`, `ApiProfile`, `Friendship`, `Friendships`), которые прячут весь SQL за `create`/`delete`/`exists` |
| `utils` | заморожен: только `RandomDataUtils` — случайные username/password; новые классы сюда не добавляются |
| `ui` | `page` — Page Objects, `component` — общие части страниц |
| `test` | только тесты, по фичам: `auth`, `profile`, позже `people`, … |

Правила содержания пакетов:

1. Пакет называется по предметной области, а не по типу класса: новых пакетов-свалок `config`, `utils`, `helpers`, `common` не заводим. Каждый класс пакета подходит под его описание в таблице выше; если класс не подходит, ему нужен другой пакет, а не исключение в таблице.
2. `config` — только `TestConfig`: читает системные свойства и переменные окружения и отдаёт значения (`String`, `URI`, `Duration`, `Path`, `boolean`). Без сети, SQL, ожиданий и объектов с поведением.
3. `stand` — что входит в стенд и как проверить его готовность и схему. Без JUnit и без SQL строк.
4. `jupiter` — только связка с JUnit и Playwright JUnit: аннотации, реализации `Extension`, `ExecutionCondition`, `OptionsFactory`, их package-private помощники (как `SuiteRun`) и probe-тесты. Расширение собирает объекты из `config`, `stand`, `data` и вызывает их; сокетов, HTTP, SQL и локаторов в нём нет.
5. `data` — тестовые данные и их владельцы, без JUnit; `data/db` — подключения и объекты строк, весь SQL спрятан за ними.
6. `ui` — только `page` и `component` по skill `rfr-page-objects`.
7. `test` — только тесты по фичам и общие сценарии фичи (как `PeopleLogin`); без локаторов и SQL.
8. `utils` заморожен: генератор нового значения оформляется объектом в пакете своей предметной области.
9. `ThreadLocal` для состояния теста не используется — состояние живёт в Store расширения.

## Агент

Page Objects пишет Claude Code по skill `rfr-page-objects`; UI исследуется через Playwright MCP (`.mcp.json`).

Доказательство корректности теста - запуск gradle таски.