Подробности — `rfr-gql-client/README.md`. Собирается отдельно от Gradle (`npm ci`, `npm run dev`); CI гоняет `npm run lint` с `--max-warnings 0`.

## Что важно знать

- Порт 3001 зашит в redirect URI клиента в `rfr-auth`, менять его нельзя.
- Каждая GraphQL-операция — отдельный хук в `src/hooks` с постоянным `operationName`. Новая операция — новый хук.
- В `rfr-api` уходит только access token, `id_token` нужен лишь для logout.
- Кэш Apollo сразу обновляет карточку ответом мутации. Успешный snackbar не доказывает сохранение: проверяй повторным чтением.
- Компоненты берутся из MUI, свои с нуля не пишутся.
