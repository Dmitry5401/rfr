package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.flywaydb.core.Flyway;

public final class SchemaMigrations {
  private SchemaMigrations() {
  }

  public static void validate() {
    List<IllegalStateException> failures = new ArrayList<>();
    for (Databases database : Databases.values()) {
      try {
        validate(database, migrations(database));
      } catch (IllegalStateException failure) {
        failures.add(failure);
      }
    }
    if (failures.isEmpty()) {
      return;
    }
    if (failures.size() == 1) {
      throw failures.getFirst();
    }
    StringBuilder text = new StringBuilder();
    for (IllegalStateException failure : failures) {
      if (!text.isEmpty()) {
        text.append("\n\n");
      }
      text.append(failure.getMessage());
    }
    IllegalStateException combined = new IllegalStateException(text.toString(), failures.getFirst());
    for (int index = 1; index < failures.size(); index++) {
      combined.addSuppressed(failures.get(index));
    }
    throw combined;
  }

  static void validate(Databases database, Path migrations) {
    if (!Files.isDirectory(migrations)) {
      throw new IllegalStateException(
          "В модуле " + database.backend() + " нет каталога миграций " + migrations
              + ". Проверка e2e сравнивает файлы текущей ветки со схемой " + database.schema() + ".");
    }
    try {
      Flyway.configure()
          .dataSource(database.dataSource())
          .locations("filesystem:" + migrations.toAbsolutePath().toString().replace('\\', '/'))
          .schemas(database.schema())
          .defaultSchema(database.schema())
          .baselineVersion("0")
          .createSchemas(false)
          .connectRetries(0)
          .load()
          .validate();
    } catch (RuntimeException error) {
      throw new IllegalStateException(message(database, error), error);
    }
  }

  static String message(Databases database, Throwable error) {
    String reason = flatten(error);
    String lower = reason.toLowerCase(Locale.ROOT);
    if (connection(lower)) {
      return "Нет соединения с MySQL для схемы " + database.schema()
          + " (" + TestConfig.dbUrl(database.schema()) + "). "
          + "Поднимите базу и дождитесь healthy: docker compose up -d --wait. Затем снова ./gradlew e2e."
          + "\nПричина: " + reason;
    }
    if (lower.contains("unknown database") || lower.contains("doesn't exist yet") || lower.contains("does not exist")) {
      return "В локальной MySQL нет схемы " + database.schema() + ". "
          + "Запустите бэкенды, чтобы они создали схему и накатили миграции текущей ветки: ./gradlew runAll. "
          + "Дождитесь старта rfr-auth и rfr-api, затем снова ./gradlew e2e."
          + "\nПричина: " + reason;
    }
    if (lower.contains("not applied to database")) {
      return "В схеме " + database.schema() + " не применены миграции из " + database.backend() + " текущей ветки. "
          + "Запустите бэкенды, чтобы Flyway докатил их: ./gradlew runAll. "
          + "Дождитесь старта rfr-auth и rfr-api, затем снова ./gradlew e2e."
          + "\nПричина: " + reason;
    }
    return "Схема " + database.schema() + " в локальной MySQL не совпадает с миграциями "
        + database.backend() + " текущей ветки. "
        + "Ветку бэкенда переключили, а контейнер MySQL не пересоздавали, поэтому код и схема разъехались. "
        + "Остановите ./gradlew runAll и выполните: "
        + "docker compose down && docker volume rm mysql-volume && docker compose up -d --wait && ./gradlew runAll. "
        + "Дождитесь старта обоих сервисов и снова ./gradlew e2e."
        + "\nПричина: " + reason;
  }

  private static Path migrations(Databases database) {
    return TestConfig.repositoryRoot()
        .resolve(database.backend())
        .resolve("src/main/resources/db/migration")
        .resolve(database.schema());
  }

  private static boolean connection(String lower) {
    return lower.contains("communications link failure")
        || lower.contains("connection refused")
        || lower.contains("could not connect")
        || lower.contains("connection is not available");
  }

  private static String flatten(Throwable error) {
    StringBuilder text = new StringBuilder();
    for (Throwable current = error; current != null; current = current.getCause()) {
      String part = current.getMessage();
      if (part == null || part.isBlank() || text.indexOf(part) >= 0) {
        continue;
      }
      if (!text.isEmpty()) {
        text.append(" | ");
      }
      text.append(part);
    }
    return text.isEmpty() ? error.toString() : text.toString();
  }
}
