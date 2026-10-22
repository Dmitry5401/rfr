Подробности — `rfr-e2e/README.md`.

- Структура пакетов — `rfr-e2e/README.md`, раздел «Архитектура». Новые классы кладутся в существующие пакеты.
- Page Objects и компоненты — по skill `rfr-page-objects` (`.claude/skills/rfr-page-objects/SKILL.md`).
- Без комментариев и Javadoc в коде `rfr-e2e` (Java, Gradle, SQL, properties): смысл передают имена и `@Step`.
- Вход в тестах только через UI; токены в localStorage не подставлять.
- Проход сценария через Playwright MCP — не запуск теста. Успех подтверждается прогоном `./gradlew :rfr-e2e:e2e --tests '<Тест>'`.
- Тесты с браузером помечаются `@Tag("ui")`, иначе задача `e2e` их не запустит.
- Тестовые данные создаются и удаляются фикстурами (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE`/`TRUNCATE`.
