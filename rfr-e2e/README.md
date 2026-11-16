# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузер Chromium
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew :rfr-e2e:test --tests '*MainPageTest'
./gradlew :rfr-e2e:test --tests io.student.rangiffler.jupiter.probe.UserExtensionTest
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

UI-тестам нужен стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README). Пробам `UserExtension` стенд целиком не нужен, см. раздел ниже.

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

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `jupiter` | всё про JUnit: `annotation` (`@User`, `@ProbeTarget`), `extension` (`UserExtension`), `condition` (`ProbeTargetCondition`), `probe` (`UserExtensionTest`) |
| `data` | тестовые данные и их владельцы: `TestUser`, `UserFixture`; `db` — `Databases`, `Jdbc` |
| `utils` | статические утилиты без состояния: `RandomDataUtils` — случайные username/password; сюда же будущие генераторы (описания фото и т.п.) |
| `ui` | `page` — Page Objects, `component` — общие части страниц |
| `test` | только тесты, по фичам: `auth`, позже `profile`, `people`, … |

Правила: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — состояние живёт в Store расширения.

- `RegistrationTest` — регистрация через UI, вход, главная, имя в профиле. Пользователь удаляется в `@AfterEach` той же фикстурой.
- `MainPageTest` — пользователь приходит параметром `@User TestUser`: `UserExtension` до теста создаёт его SQL-скриптами в `rangiffler-auth` (`UserFixture.create()`), после теста удаляет профиль API, authority и user (`close()` через Store).
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
нужны. `./gradlew e2e` запускает эти 9 тестов вместе с UI-тестами, поэтому без мигрированной базы упадут и они.

Проверяется следующее:

- `@User` на параметре теста создаёт пользователя до тела теста. В репорте имя, число друзей и входящих заявок, наличие учётки и профиля.
- Друг записывается со статусом `ACCEPTED`, входящая заявка со статусом `PENDING`.
- Два параметра `@User` получают разных пользователей: у каждого свой ключ в Store.
- После теста `StoredUser` закрывает фикстуру. То же происходит, если тест упал: репорт уже записан, профиль и учётка удалены.
- `friends = -1` роняет `beforeEach` до тела теста, записей в репорте нет.
- `TestUser` без `@User` и `@User` на параметре `@BeforeEach` не резолвятся.

Вложенные цели (`Observed`, `Failing`, `TwoUsers` и остальные) сами не являются тестовыми классами `*Test`. `@ProbeTarget` включает их только когда `EngineTestKit` передаёт конфигурационный параметр `rfr.probe=true`. Это не свойство `-Drfr.*` и не переменная `TestConfig`: снаружи цели остаются выключенными, и обычный прогон их не исполняет.

## Агент

Page Objects пишет Claude Code по skill `rfr-page-objects`; UI исследуется через Playwright MCP (`.mcp.json`). Домашнее задание — `docs/lessons/01/homework/README.md`. Проход через MCP — не запуск теста: подтверждение только JUnit-прогоном.
