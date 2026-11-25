# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузер Chromium
./gradlew e2e                                             # UI через E2eSuite (@SelectPackages + @IncludeTags ui)
./gradlew :rfr-e2e:test                                   # всё без тега ui; стенд не нужен
./gradlew :rfr-e2e:test --tests '*RegistrationTest'       # с --tests тег ui не отсекается
./gradlew :rfr-e2e:test --tests io.student.rangiffler.jupiter.probe.UserExtensionTest
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

UI-тестам нужен стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README). Перед первым классом с тегом `ui` `StandReadinessExtension` ждёт стенд (те же признаки, что `scripts/wait-for-stand.sh`) не дольше `rfr.standTimeout` секунд — и для `e2e`, и для запуска из IDE. Задача `waitForStand` ждёт стенд отдельно; `e2e` от неё не зависит. Пробам `UserExtension` стенд целиком не нужен, см. раздел ниже.

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
| `rfr.viewportWidth` / `rfr.viewportHeight` | `1920` / `1080` — viewport в `BrowserOptions` |
| `rfr.testIdAttribute` | `data-test-id` — атрибут для `getByTestId` |
| `rfr.actionTimeout` | `5` (секунды) — Playwright default timeout для click/fill/assert; не JUnit `@Timeout` |
| `rfr.navigationTimeout` | `30` (секунды) — Playwright default navigation timeout |
| `rfr.standCheck` | `true` — ждать стенд перед UI-тестами |
| `rfr.standTimeout` | `180` (секунды) |
| `rfr.migrationCheck` | `false`, задача `e2e` включает |
| `rfr.commit` | короткий hash `HEAD`, Gradle выставляет сам (`git rev-parse --short HEAD`) |

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | только `TestConfig` — единственная точка чтения настроек |
| `stand` | готовность и схема стенда: `Stand` / `Service` (`TcpService`, `HttpService`), `Migrations` |
| `jupiter` | всё про JUnit: `annotation` (`@UiTest`, `@User` / `@StaticUser` — маркеры параметров; `@ProbeTarget`), `extension` (`BrowserOptions` — `OptionsFactory` для `@UsePlaywright`; `BrowserTimeoutExtension` — Playwright action timeout из `TestConfig`; `UserExtension` / `UsersQueueExtension` — через `@ExtendWith` на классе; через ServiceLoader: `AllureEnvironmentExtension`, затем `StandReadinessExtension`, затем `StandSchemaExtension`), `condition` (`ProbeTargetCondition`), `probe` |
| `suite` | `E2eSuite` — JUnit Platform Suite (`@SelectPackages` + `@IncludeTags("ui")`); задача `e2e` выбирает этот класс |
| `data` | тестовые данные и их владельцы: `TestUser`, `UserFixture`; `db` — `Databases`, `Jdbc` |
| `utils` | статические утилиты без состояния: `RandomDataUtils` — случайные username/password; сюда же будущие генераторы (описания фото и т.п.) |
| `ui` | `page` — Page Objects, `component` — общие части страниц |
| `test` | только тесты, по фичам: `auth`, позже `profile`, `people`, … |

Правила: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — состояние живёт в Store расширения.

- `RegistrationTest` — регистрация через UI, вход, главная, имя в профиле. Пользователь удаляется в `@AfterEach` той же фикстурой.
- `MainPageTest` — класс регистрирует `@ExtendWith(UserExtension.class)`; параметр `@User TestUser` — маркер. Extension до теста создаёт пользователя SQL-скриптами в `rangiffler-auth` (`UserFixture.create()`), после теста удаляет профиль API, authority и user (`close()` через Store).
- `E2eSuite` — один вход для UI-набора: `./gradlew e2e` фильтрует suite-класс; SuiteExtension (ServiceLoader) по-прежнему готовит стенд/схему/Allure.
- `UserExtensionTest` — проба архитектуры расширения через `EngineTestKit` и `TestReporter`. Браузер не открывает.

Как JUnit вызывает эти методы и где хранится fixture — занятие 3. SQL и транзакции — занятие 17.

## Пробы UserExtension

Класс `io.student.rangiffler.jupiter.probe.UserExtensionTest`, тег `engine-probe`. `EngineTestKit` поднимает отдельный
прогон вложенного класса с `@ProbeTarget`. Тело пробы пишет в `TestReporter`, что расширение положило в параметр.
Внешний тест читает эти записи и сверяет их с базой уже после того, как Store закрыл фикстуру.

```bash
./gradlew :rfr-e2e:test --tests io.student.rangiffler.jupiter.probe.UserExtensionTest
```

Перед первым запуском нужен MySQL и применённый Flyway. Достаточно один раз поднять оба бэкенда, чтобы появились схемы `rangiffler-auth` и `rangiffler-api` и справочник стран:

```bash
docker compose up -d
docker compose ps          # у mysql статус healthy
./gradlew runAll           # после Started у обоих сервисов процесс можно остановить
```

Дальше сервисы можно остановить: пробы ходят в базу сами через `JdbcClient`. Фронт и `installPlaywright` для них не
нужны. `./gradlew e2e` запускает эти 11 тестов вместе с UI-тестами, поэтому без мигрированной базы упадут и они.

Проверяется следующее:

- `@User` на параметре теста создаёт пользователя до тела теста. В репорте имя, число друзей, входящих и исходящих заявок, наличие учётки и профиля.
- Друг записывается со статусом `ACCEPTED`, входящая и исходящая заявки — со статусом `PENDING`. Исходящая идёт от основного пользователя к приглашённому.
- Два параметра `@User` получают разных пользователей: у каждого свой ключ в Store.
- После теста `StoredUser` закрывает фикстуру. То же происходит, если тест упал: репорт уже записан, профиль и учётка удалены.
- `outcomeInvitations = 1` пишет `Friendship` от пользователя к приглашённому со статусом `PENDING`. После теста нет ни этой связи, ни профиля приглашённого.
- `outcomeInvitations = -1` роняет тест с `IllegalArgumentException` до создания пользователя, записей в репорте нет.
- `friends = -1` роняет `beforeEach` до тела теста, записей в репорте нет.
- `TestUser` без `@User` и `@User` на параметре `@BeforeEach` не резолвятся.
- `@User` на неверном типе (`String`) не создаёт пользователя: `beforeEach` использует ту же проверку, что `supportsParameter`.

Вложенные цели (`Observed`, `Failing`, `TwoUsers` и остальные) сами не являются тестовыми классами `*Test`. `@ProbeTarget` включает их только когда `EngineTestKit` передаёт конфигурационный параметр `rfr.probe=true`. Это не свойство `-Drfr.*` и не переменная `TestConfig`: снаружи цели остаются выключенными, и обычный прогон их не исполняет.

## Агент

Page Objects пишет Claude Code по skill `rfr-page-objects`; UI исследуется через Playwright MCP (`.mcp.json`). Домашнее задание — `docs/lessons/01/homework/README.md`. Проход через MCP — не запуск теста: подтверждение только JUnit-прогоном.
