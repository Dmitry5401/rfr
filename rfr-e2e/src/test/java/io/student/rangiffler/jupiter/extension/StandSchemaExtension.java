package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.stand.Migrations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class StandSchemaExtension implements SuiteExtension {
  @Override
  public void beforeSuite(ExtensionContext context) {
    if (TestConfig.migrationCheck()) {
      List<String> mismatches = Arrays.stream(Databases.values())
          .map(Migrations::new)
          .map(Migrations::mismatch)
          .flatMap(Optional::stream)
          .toList();
      if (!mismatches.isEmpty()) {
        throw new IllegalStateException(
            "Local database schema does not match the backend migrations of the current branch, "
                + "restart the backends with './gradlew runAll' so Flyway applies pending migrations, "
                + "and if the mismatch remains recreate the database with "
                + "'docker compose down -v && docker compose up -d --wait' and restart the backends again"
                + System.lineSeparator()
                + String.join(System.lineSeparator(), mismatches));
      }
    }
  }
}
