Подробности — `rfr-auth/README.md`. Проверка, что сервис поднят: `curl http://localhost:9001/.well-known/openid-configuration`.

## Что важно знать

- Регистрация создаёт пользователя только в схеме `rangiffler-auth`. Профиль в `rangiffler-api` появляется при первом `Query.user`.
- `POST /register` требует CSRF-токен той же сессии: сначала `GET /register`. Успех — HTTP 201, ошибка валидации или занятый username — HTTP 400.
- `GET /login` без сохранённого OAuth-запроса уводит на фронтенд. Вход начинается с `/oauth2/authorize` или кнопки Login на фронтенде.
- Ключ подписи RSA, OAuth-авторизации и сессии живут в памяти. После перезапуска все выданные токены невалидны.
