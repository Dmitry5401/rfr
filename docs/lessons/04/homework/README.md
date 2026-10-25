# Домашнее задание к занятию 04. JUnit 6 — регистрация extension'ов и suite

Результат сдается пулл-реквестом в ваш форк `rfr`.

## Код занятия

Начать ДЗ можно одним из двух способов.

**Из бандла.** Бандлы предыдущих занятий уже применены. Подтяните код занятия из бандла `rfr-l04.bundle` (лежит в материалах занятия) и создайте от него рабочую ветку:

```bash
git fetch ./rfr-l04.bundle v3/l04-junit-live:v3/l04-junit-live
git switch -c hw/04-junit-suite v3/l04-junit-live
```

**Вручную, без бандла.** Повторите за преподавателем все, что было написано на занятии, в своей ветке от результата предыдущего ДЗ. Перед началом задач ваш проект должен совпадать с состоянием на конец занятия: те же классы, настройки и зеленые тесты. Если что-то не сходится, сверьтесь с бандлом или записью занятия, иначе задачи могут не лечь на ваш код.

## Задачи

1. **`StandSchemaExtension` через `@BeforeSuite`.** Перенесите сверку миграций в `@BeforeSuite` класса `@Suite` из `junit-platform-suite`, а `StandSchemaExtension` уберите из `META-INF/services`. Задача `e2e` должна запускать тесты через suite-класс. Проверьте и запишите, что происходит в каждом из случаев:
   - сколько раз выполняются тесты в `./gradlew e2e`, если не ограничивать движки;
   - выполняется ли `@BeforeSuite` при запуске одного класса или одного метода из IDE и при `./gradlew e2e --tests '*TabTest'`;
   - как падение `@BeforeSuite` выглядит в выводе Gradle и в Allure.

   Сделайте попытку отдельным коммитом и откатите ее следующим коммитом через `git revert`. 1,5 ч.
2. **`StandSchemaExtension` через `LauncherSessionListener`.** Реализуйте ту же сверку в `launcherSessionOpened` и зарегистрируйте слушатель в `META-INF/services/org.junit.platform.launcher.LauncherSessionListener`. Для компиляции `junit-platform-launcher` понадобится в `testImplementation`. Проверьте и запишите:
   - срабатывает ли слушатель в `./gradlew :rfr-e2e:test`, где UI-тестов нет, и как вы это отличаете;
   - сколько раз он срабатывает при `maxParallelForks = 2`;
   - срабатывает ли он внутри `EngineTestKit` в `SuiteExtensionTest`;
   - как Gradle и IDE показывают исключение из `launcherSessionOpened`.

   Тоже отдельный коммит и `git revert`. В описании PR — таблица «`SuiteExtension` / `@BeforeSuite` / `LauncherSessionListener`»: когда срабатывает, видит ли запуск из IDE, знает ли о `Store` и тегах, как падает. Под таблицей — одна фраза о том, почему в проекте остается `SuiteExtension`, или аргументы за смену подхода. 1,5 ч.
3. **`StandReadinessExtension`: ждем стенд, а не падаем сразу.** Реализация `SuiteExtension`, которая в `beforeSuite` опрашивает MySQL, `rfr-auth`, `rfr-api` и фронт по тем же признакам, что `scripts/wait-for-stand.sh`. Адреса берутся из `TestConfig`. Опрос повторяется, пока все четыре сервиса не ответят или не истечет таймаут `rfr.standTimeout` (по умолчанию 180 с). Каждый запрос ограничен своим таймаутом, бесконечного ожидания нет нигде. Если таймаут истек, сообщение называет сервисы, которые так и не ответили, и команду, которая их поднимает (`docker compose up -d --wait`, `./gradlew runAll`, `npm run dev`). Требования:
   - ожидание запускается перед первым классом с тегом `ui`, поэтому стенд ждут и `e2e`, и UI-тест, запущенный из IDE; тесты без тега `ui` к стенду не обращаются, а свойство `rfr.standCheck=false` выключает ожидание совсем;
   - `StandReadinessExtension` выполняется раньше `StandSchemaExtension`, потому что сверке миграций нужна MySQL; порядок задается в `META-INF/services`, и в PR объясните, почему он гарантирован;
   - `e2e` больше не зависит от `waitForStand`, а UI-тест, запущенный из IDE при выключенном фронте, ждет стенд и падает с понятной ошибкой, а не с таймаутом Playwright.

   Мишени `@ProbeTarget` в `StandReadinessExtensionTest` через EngineTestKit, с подставным HTTP-сервером (`com.sun.net.httpserver.HttpServer`) на эфемерном порту: «сервис начал отвечать через 2 с — тесты выполняются», «сервис не ответил за таймаут — тесты не выполняются, а в ошибке есть имя сервиса», «класс без тега UI-тестов — к сервису нет ни одного запроса», «проверка выключена — к сервису нет ни одного запроса». 2 ч.
4. **`AllureEnvironmentExtension`: окружение прогона в отчете.** Реализация `SuiteExtension`, которая в `afterSuite` пишет `environment.properties` в каталог результатов Allure (`allure.results.directory`, по умолчанию `rfr-e2e/build/allure-results`). В файле: `frontUrl`, `authUrl`, `apiUrl`, `browser`, `headless`, короткий хеш коммита и текущая версия миграций каждой схемы из Flyway. Хеш коммита передает Gradle системным свойством `rfr.commit`, тест не запускает `git` сам. Файл пишется и тогда, когда `beforeSuite` другой реализации упал: проверьте, вызывает ли JUnit `beforeAll` следующих extension'ов после упавшего, и решите, на каком месте в `META-INF/services` должна стоять `AllureEnvironmentExtension`. Если MySQL недоступна, версия схемы в файле — `unavailable`, и файл все равно пишется. Мишень в `AllureEnvironmentExtensionTest`: «после прогона во временном каталоге результатов есть `environment.properties` со всеми ключами». В PR — скриншот блока Environment в отчете `./gradlew allureServe`. 1 ч.

## AI в ДЗ

Задачи 3 и 4 студент делает с агентом, ревью — `test-reviewer`; каждое принятое замечание закрыто мишенью. В PR — вывод `test-reviewer`. Правило 13 skill `rfr-test-review` (реализации `SuiteExtension` — только через ServiceLoader) должно распознать регистрацию `StandReadinessExtension` через `@ExtendWith`: проверьте это на временной правке и приложите замечание агента.

## Definition of Done

В `META-INF/services/org.junit.jupiter.api.extension.Extension` — `AllureEnvironmentExtension`, `StandReadinessExtension` и `StandSchemaExtension` в порядке, обоснованном в PR, ни одна из них не подключена через `@ExtendWith`; `e2e` не зависит от `waitForStand`; `:rfr-e2e:test` не обращается к стенду; попытки из задач 1 и 2 есть в истории PR и откачены; мишени `@ProbeTarget` не выполняются вне своих тестовых классов.

## Как проверяем

`./gradlew :rfr-e2e:test` ×2 и `./gradlew e2e` ×2 на поднятом стенде. Затем `./gradlew e2e` с остановленным фронтом: прогон ждет, а если за это время запустить `npm run dev`, проходит. Если фронт не запускать, прогон падает с `rfr.standTimeout=10`, и в ошибке названо имя `rfr-gql-client`. Красная проверка «убрать повтор опроса» — мишень «сервис начал отвечать через 2 с» красная. Красная проверка «переставить `AllureEnvironmentExtension` после `StandSchemaExtension` и сломать схему» — `environment.properties` не появился; на своем месте в `META-INF/services` он появляется и при сломанной схеме, и при остановленной MySQL. В PR — таблица из задачи 2, вывод `test-reviewer` и скриншот блока Environment.
