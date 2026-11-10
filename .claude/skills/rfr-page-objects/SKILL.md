---
name: rfr-page-objects
description: Создаёт и меняет Page Objects и компоненты UI Rangiffler на Playwright Java в rfr-e2e (пакеты ui/page и ui/component). Применять к страницам, локаторам, действиям и проверкам UI. Фикстуры, SQL и JUnit extensions — вне ответственности страниц.
---

# Page Objects Rangiffler

Страницы — `rfr-e2e/src/test/java/io/student/rangiffler/ui/page`, общие части — `ui/component`, адреса — `config/TestConfig`.
Доступные имена и роли элементов подтверждай в браузере через Playwright MCP (`http://localhost:3001`), не угадывай по исходникам фронта.

## Проверка результата

Прогон `./gradlew :rfr-e2e:test --tests '<Тест>' --rerun-tasks` — единственное подтверждение. Проход сценария через MCP запуском теста не считается.
В отчёте разделяй: что подтверждено в браузере через MCP, что взято из исходников.

## Практика занятия 01

### MUI Select: option фильтруется по вложенному IMG
Поле вроде Location — MUI Select с ролью combobox. Доступное имя пункта бывает составным (`France France`), поэтому `getByRole(OPTION)` с `setName(country).setExact(true)` пункт не находит. Действие выбора страны:
- клик по `page.getByRole(AriaRole.COMBOBOX, … setName("Location").setExact(true))`;
- клик по `page.getByRole(AriaRole.OPTION).filter(… setHas(page.getByRole(AriaRole.IMG, … setName(country).setExact(true))))`.

Список опций рисуется отдельным оверлеем, поэтому локатор `option` берётся у `page`, а не у локатора закрытого combobox. Точное имя страны задаётся у вложенного `IMG`, а не у самого `option`. В тест передаётся одно и то же название страны. Проверка читает этот же флаг внутри combobox: `location.getByRole(IMG, … setName(country).setExact(true))`.

### Ожидание ответа мутации — внутри действия
`clickSave()` кликает Save внутри `page.waitForResponse(...)`: URL содержит `/graphql`, тело запроса содержит `UpdateUser`, ответ успешный. Тест вызывает `clickSave()` и сразу `checkThat...`. В тесте нет своего `waitForResponse` и нет `waitForTimeout`. Аватар после сохранения — `img`, чей `src` равен data URL загруженного файла. Ожидаемая строка приходит из теста в `checkThatAvatarSaved(avatarSrc)`.

## Контекст проекта

Учебный проект Rangiffler. Регистрация, вход, профиль и дружба настоящие. Фото, лайки, лента и статистика возвращаются из JSON-моков `rfr-api/src/main/resources/mock/*.json`.

| Модуль | Что это | Порт |
|---|---|---|
| `rfr-auth` | Spring Authorization Server, схема `rangiffler-auth` | 9001 |
| `rfr-api` | GraphQL API `/graphql`, схема `rangiffler-api`; фото и лента — моки | 8081 |
| `rfr-gql-client` | React + Apollo, собирается отдельно от Gradle (`npm run dev`) | 3001 |
| `rfr-e2e` | E2E-тесты: JUnit 6, Playwright Java, Allure 3 | — |

MySQL — порт 3306, пользователь `root`, пароль `secret`.

## Окружение

Стенд: JDK 25, Node 22+, Docker. Не запускай `localenv.sh`: он останавливает и удаляет все Docker-контейнеры машины. Поднимай только MySQL (`docker run --name rfr-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=secret -d mysql:8.4.7`), затем сервисы и фронт.