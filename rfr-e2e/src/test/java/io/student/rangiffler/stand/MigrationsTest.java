package io.student.rangiffler.stand;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("schema-migrations")
@Execution(ExecutionMode.SAME_THREAD)
class MigrationsTest {

  @Test
  void currentBranchMatchesTheLocalDatabase() {
    for (Databases database : Databases.values()) {
      var mismatch = new Migrations(database).mismatch();
      assertTrue(mismatch.isEmpty(), () -> database.schema() + " " + mismatch.orElse(""));
    }
  }

  @Test
  void pendingMigrationIsReported() throws IOException {
    Path migrations = copy(Databases.AUTH);
    Files.writeString(migrations.resolve("V99__pending.sql"), "select 1;\n");

    String mismatch = new Migrations(Databases.AUTH, migrations).mismatch().orElse("");

    assertTrue(mismatch.contains("rangiffler-auth"), mismatch);
    assertTrue(mismatch.contains("not applied"), mismatch);
  }

  @Test
  void changedMigrationIsReported() throws IOException {
    Path migrations = copy(Databases.AUTH);
    Files.writeString(migrations.resolve("V1__schema_init.sql"), "\n-- drift\n", StandardOpenOption.APPEND);

    String mismatch = new Migrations(Databases.AUTH, migrations).mismatch().orElse("");

    assertTrue(mismatch.contains("rangiffler-auth"), mismatch);
    assertTrue(mismatch.contains("checksum"), mismatch);
  }

  private static Path copy(Databases database) throws IOException {
    Path source = TestConfig.migrations(database.schema());
    Path copy = Files.createTempDirectory("rfr-" + database.schema());
    try (var files = Files.list(source)) {
      for (Path file : files.toList()) {
        if (Files.isRegularFile(file)) {
          Files.copy(file, copy.resolve(file.getFileName()));
        }
      }
    }
    return copy;
  }
}
