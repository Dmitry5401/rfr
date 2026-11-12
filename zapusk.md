# Запуск стенда Rangiffler

Для агента полный контекст проекта — skill `rfr` (`.claude/skills/rfr/SKILL.md`).
Ниже — краткая шпаргалка.

Требования: JDK 25, Node.js 22+, Docker Desktop (демон запущен).

```bash
docker compose up -d          # MySQL :3306, root/secret; дождись healthy
./gradlew runAll              # auth :9001, api :8081
cd rfr-gql-client && npm ci && npm run dev   # :3001
./gradlew e2e                 # все тесты rfr-e2e
```

## Ошибка: порт 3306 занят (`docker compose up -d`)

Если Docker пишет что-то вроде:

```text
ports are not available: ... bind: Only one usage of each socket address ... 3306
```

на хосте уже слушает локальный MySQL (`mysqld.exe`). Освободите порт в **PowerShell** (не Git Bash — там `/FI` ломается).

```powershell
# из корня репо (пример пути)
cd C:\Users\<user>\Projects\qa_guru_2026_advanced\rfr

# кто держит 3306
netstat -ano | findstr :3306
tasklist /FI "PID eq <PID_из_netstat>"

# остановить службу MySQL (часто MySQL80)
Get-Service *mysql*
net stop MySQL80
# или: Stop-Service MySQL80

# порт свободен?
netstat -ano | findstr :3306

docker compose up -d
```

Если `net stop` не находит службу — возьмите имя из `Get-Service *mysql*` и остановите его, либо `Stop-Process -Id <PID> -Force`.

## Остановка

В терминале, где крутится процесс:

- `./gradlew runAll` → **Ctrl+C** (auth :9001 и api :8081)
- `npm run dev` → **Ctrl+C** (фронт :3001)

Если порты остались заняты (из корня репо):

```bash
bash ports.sh                 # kill процессов на 9001, 8081, 3001
```

MySQL:

```bash
docker compose down           # контейнер rfr-mysql; volume mysql-volume сохраняется
```
