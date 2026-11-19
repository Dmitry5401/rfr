package io.student.rangiffler.data.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.student.rangiffler.config.TestConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.Test;

class SchemaMigrationsTest {

  @Test
  void currentBranchMatchesTheLocalDatabase() {
    SchemaMigrations.validate();
  }

  @Test
  void pendingMigrationTellsToStartTheBackends() throws IOException {
    Path migrations = copy(Databases.AUTH);
    Files.writeString(migrations.resolve("V99__pending.sql"), "select 1;\n");

    IllegalStateException error = assertThrows(
        IllegalStateException.class,
        () -> SchemaMigrations.validate(Databases.AUTH, migrations));

    assertTrue(error.getMessage().contains("rangiffler-auth"), error.getMessage());
    assertTrue(error.getMessage().contains("./gradlew runAll"), error.getMessage());
    assertFalse(error.getMessage().contains("docker volume rm"), error.getMessage());
  }

  @Test
  void changedMigrationTellsToRecreateTheVolume() throws IOException {
    Path migrations = copy(Databases.AUTH);
    Files.writeString(
        migrations.resolve("V1__schema_init.sql"),
        "\n-- drift\n",
        StandardOpenOption.APPEND);

    IllegalStateException error = assertThrows(
        IllegalStateException.class,
        () -> SchemaMigrations.validate(Databases.AUTH, migrations));

    assertTrue(error.getMessage().contains("rangiffler-auth"), error.getMessage());
    assertTrue(error.getMessage().contains("docker volume rm mysql-volume"), error.getMessage());
    assertTrue(error.getMessage().contains("./gradlew e2e"), error.getMessage());
  }

  @Test
  void checksumTextTellsToRecreateTheVolume() {
    String text = SchemaMigrations.message(
        Databases.AUTH,
        new RuntimeException("Migration checksum mismatch for migration version 1"));

    assertTrue(text.contains("docker volume rm mysql-volume"));
    assertTrue(text.contains("rangiffler-auth"));
  }

  @Test
  void pendingTextTellsToStartTheBackends() {
    String text = SchemaMigrations.message(
        Databases.API,
        new RuntimeException("Detected resolved migration not applied to database: 2"));

    assertTrue(text.contains("./gradlew runAll"));
    assertTrue(text.contains("rangiffler-api"));
    assertFalse(text.contains("docker volume rm"));
  }

  @Test
  void refusedConnectionTellsToStartMysql() {
    String text = SchemaMigrations.message(
        Databases.AUTH,
        new RuntimeException("Communications link failure"));

    assertTrue(text.contains("docker compose up -d --wait"));
    assertTrue(text.contains("rangiffler-auth"));
  }

  private static Path copy(Databases database) throws IOException {
    Path source = TestConfig.repositoryRoot()
        .resolve(database.backend())
        .resolve("src/main/resources/db/migration")
        .resolve(database.schema());
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
