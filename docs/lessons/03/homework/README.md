# Домашнее задание к занятию 03. JUnit 6 — lifecycle, Store, @User, предусловия

Объём — 6 ч. Результат сдаётся пулл-реквестом в ваш форк `rfr`.

## Код занятия

Бандлы предыдущих занятий уже применены. Подтяните код занятия из бандла `rfr-l03.bundle` (лежит в материалах занятия) и создайте от него рабочую ветку:

```bash
git fetch ./rfr-l03.bundle v3/l03-junit-live:v3/l03-junit-live
git switch -c hw/03-junit-live v3/l03-junit-live
```

## Задачи

1. `outcomeInvitations` в `@User`: компонент `UserRequest`, `TestUser.outcomeInvitations` + `withOutcomeInvitations`, новый декоратор `UserWithOutcomeInvitationsFixture` (приглашённые — `SingleUserFixture`, связь `Friendship(user, invitee, PENDING)`, requester — основной пользователь), подключение в `UserRequest.fixture()`. `SingleUserFixture`, другие декораторы и `UserExtension` не меняются. 1 ч.
2. Тесты вкладок People на `@User`: `test/people/{FriendsTabTest, IncomeInvitationsTabTest, OutcomeInvitationsTabTest}` — состав и отсутствие чужих связей, кнопки (Remove / Accept + Decline / Waiting… + Cancel — по наличию, не нажимаются), пустое состояние. Недостающие методы вкладок и строк — в `PeoplePage` по skill `rfr-page-objects`. Минимальные предусловия: `friends = 1, incomeInvitations = 1`, не 5 пользователей на тест. 2,5 ч.
3. Две мишени в `UserExtensionTest`: «`outcomeInvitations = 1` — во время теста есть `Friendship(user, invitee, PENDING)`, после теста нет ни связи, ни профиля приглашённого», «`outcomeInvitations = -1` → `IllegalArgumentException` до создания пользователя». 1 ч.
4. Skill `rfr-test-data` v1 (≤ 30 строк): уникальность данных, владелец — дерево декораторов, новое предусловие — новый декоратор, SQL только в объектах строк `data/db`, профиль API создаётся явно, пул заранее заведённых пользователей — только там, где создать нельзя. 0,5 ч.
5. `UsersQueueExtension`: допустим, создавать пользователей на лету нельзя, и есть пул из трёх заранее заведённых тестовых пользователей. Аннотация `@StaticUser` на параметре `TestUser` подключает `UsersQueueExtension`. Аналог `SingleUserFixture` — `StaticUserFixture implements UserFixture`: `create()` берёт пользователя из общей очереди, `close()` возвращает в неё только взятого пользователя и ничего не удаляет из БД. Очередь `static`, одна на всю JVM, потокобезопасная; при пустой очереди `create()` ждёт с таймаутом и падает с понятной ошибкой, а не висит. Extension кладёт фикстуру в Store до `create()`, поэтому пользователь возвращается при закрытии Store, в том числе после упавшего теста. В очереди могут быть любые пары username/password: заводить их в стенде не нужно. Мишени в `UsersQueueExtensionTest` через EngineTestKit: «четыре теста подряд на пуле из трёх зелёные», «после упавшего теста пользователь вернулся в очередь». `UserExtension` и `UserRequest` не меняются. 1 ч.


## Идея для рефакторинга (по желанию, вне 6 ч)

`UserWithFriendsFixture`, `UserWithIncomeInvitationsFixture` и `UserWithOutcomeInvitationsFixture` почти одинаковы: создают `count` пользователей через `SingleUserFixture`, связывают каждого с основным и закрывают в том же порядке. Различаются они тремя вещами: статусом `Friendship` (`ACCEPTED` или `PENDING`), направлением связи (кто requester, кто addressee) и методом `TestUser`, в который попадают связанные пользователи. Эти различия можно вынести в enum типа связи, например `Relation { FRIEND, INCOME_INVITATION, OUTCOME_INVITATION }`. Тогда вместо трёх декораторов останется один `UserWithRelationsFixture(origin, relation, count)`, а `UserRequest.fixture()` будет оборачивать фикстуру им для каждого ненулевого счётчика. Рефакторинг делается после задачи 1, мишени `UserExtensionTest` остаются зелёными без изменений. Если вы его сделаете, поправьте правило «новое предусловие — новый декоратор» в skill `rfr-test-data`: для новой связи достаточно нового значения enum.

## AI в ДЗ

Предусловия и тесты студент делает с агентом, ревью — `test-reviewer`; каждое принятое замечание закрыто тестом или мишенью. В PR — вывод `test-reviewer` и одно правило, добавленное в `rfr-test-review`.


## Definition of Done

Ни один тест, кроме тестов на `@StaticUser`, не опирается на пользователей, созданных вне него; после прогона в очереди снова все три пользователя пула; после прогона в `friendship` нет строк с участием тестовых пользователей; мишени `@ProbeTarget` не выполняются вне `UserExtensionTest`; `UserExtensionTest` зелёный без браузера.


## Как проверяем

`./gradlew :rfr-e2e:test` ×2 и `:rfr-e2e:e2e --tests '*TabTest'` ×2; красная «убрать `put`» — мишени `Observed`/`Failing` красные; красная «убрать возврат в `close()`» — мишень «четыре теста подряд» красная; `SELECT` по `rfr_%` в `friendship`, `user` обеих схем = 0; в PR — вывод `test-reviewer`.
