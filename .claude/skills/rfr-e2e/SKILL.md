---
name: rfr-e2e
description: Создаёт и меняет код модуля rfr-e2e, кроме Page Objects. Применять к тестам, фикстурам, TestConfig, JUnit-расширениям и SQL ресурсов. Страницы и компоненты UI — skill rfr-page-objects.
---

# Модуль rfr-e2e

E2E-тесты Rangiffler: JUnit 6, Playwright Java (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур. Один source set `src/test`, корневой пакет `io.student.rangiffler`.

## Конфигурация Настройки тестов

`TestConfig` читает `-Drfr.<ключ>=…` → переменная `RFR_<КЛЮЧ>` → значение по умолчанию. Gradle передаёт все `-Drfr.*` в тестовую JVM. Адреса и учётка БД не хардкодятся в тестах и фикстурах.

## Создание Page Objects

Для `ui/page` и `ui/component` применяй skill `rfr-page-objects`.

## Архитектура проекта

1. Класс `final`, поля `private final`: `Page`, `Locator`, компоненты. Исключение — `public static final String URL` с базой из `TestConfig`; он есть у каждой страницы, даже если на неё попадают только переходом.
2. Конструктор принимает только `Page` и только присваивает поля: без переходов, кликов, проверок, ожиданий и SQL.
3. Композиция вместо наследования: общие части (`CredentialsForm`, `HeaderComponent`) — компоненты с тем же `Page`, базовых классов нет.
4. Каждый публичный метод страницы и компонента — с `@Step`, и каждый параметр попадает в текст шага: `@Step("Заполнить форму входа: {username} / {password}")`.
5. Переход возвращает объект следующей страницы, действие и проверка на текущей — `this`.
6. Одно бизнес-действие — один метод, имя которого называет форму: `fillLoginForm(username, password)`, `fillRegistrationForm(...)`. Без безликих `fillForm`/`fill`/`submit` и цепочек `fillUsername().fillPassword()`; отдельный метод на поле — только если тесту нужно именно поле (валидация).
7. Действие отдельно от проверки: `fill…`/`click…` ничего не проверяют; `checkThat…` только проверяют через `PlaywrightAssertions.assertThat(locator)` и ничего не кликают. Без `locator.isVisible()` в JUnit-проверках и ручных ожиданий.
8. Без `checkThatOpened`, `checkPageLoaded`, `isLoaded`, `waitUntilLoaded`: `click`/`fill` ждут элемент, web-first assertions ждут состояние. URL и видимость страницы проверяем, только когда переход и есть предмет теста.
9. Локаторы: `getByRole` с точным `name`, `getByLabel`, `getByPlaceholder`, `getByText` с точным совпадением, `getByTestId`. Без `first()`/`nth()`, MUI-классов и XPath; неуникальный элемент сужай родителем: `page.getByRole(AriaRole.BANNER).getByRole(…)`.
10. Проверка не должна быть тавтологией локатора: `assertThat(heading("Travels map")).hasText("Travels map")` ничего не доказывает. Ожидаемое значение приходит из теста (`checkThatUsername(username)`).
11. Вход только через UI; `access_token`/`id_token` в `localStorage` не подставлять (до занятия 13).
12. Без комментариев и Javadoc: смысл передают имена методов и текст `@Step`.

Новые классы кладутся в существующие пакеты. Описание — `rfr-e2e/README.md`, раздел «Архитектура».

## Пакеты

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


| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схему добавляет код) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` | `chromium` / `true` |

## Команды

```bash
./gradlew :rfr-e2e:installPlaywright
./gradlew :rfr-e2e:test --tests '*RegistrationTest' --rerun-tasks
./gradlew :rfr-e2e:test --tests '*MainPageTest' --rerun-tasks
./gradlew :rfr-e2e:allureReport
```

`installPlaywright` ставит Chromium один раз. Отчёт: `rfr-e2e/build/reports/allure-report/allureReport/index.html`. Проход сценария через Playwright MCP запуском теста не считается.