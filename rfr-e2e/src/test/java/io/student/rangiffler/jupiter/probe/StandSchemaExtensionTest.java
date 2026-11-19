package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;

@Isolated
@ResourceLock("schema-migrations")
@Execution(ExecutionMode.SAME_THREAD)
class StandSchemaExtensionTest {

  @Test
  void registeredThroughServiceLoader() throws IOException {
    String listing;
    try (InputStream in = getClass().getResourceAsStream(
        "/META-INF/services/org.junit.jupiter.api.extension.Extension")) {
      listing = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    assertTrue(listing.contains(StandSchemaExtension.class.getName()), listing);
    assertTrue(ServiceLoader.load(Extension.class).stream()
        .anyMatch(provider -> provider.type().equals(StandSchemaExtension.class)));
  }

  @Test
  void staysQuietWhenTheE2eTaskDidNotAsk() {
    withProperties("false", null, () -> new StandSchemaExtension().beforeSuite(null));
  }

  @Test
  void mismatchNamesTheRepairCommands() throws IOException {
    Path root = Files.createTempDirectory("rfr-project");
    for (Databases database : Databases.values()) {
      Path source = TestConfig.migrations(database.schema());
      Path target = root
          .resolve("rfr-" + database.schema().substring("rangiffler-".length()))
          .resolve("src/main/resources/db/migration")
          .resolve(database.schema());
      Files.createDirectories(target);
      try (var files = Files.list(source)) {
        for (Path file : files.toList()) {
          if (Files.isRegularFile(file)) {
            Files.copy(file, target.resolve(file.getFileName()));
          }
        }
      }
    }
    Files.writeString(
        root.resolve("rfr-auth/src/main/resources/db/migration/rangiffler-auth/V99__pending.sql"),
        "select 1;\n");

    IllegalStateException error = assertThrows(
        IllegalStateException.class,
        () -> withProperties("true", root.toString(), () -> new StandSchemaExtension().beforeSuite(null)));

    assertTrue(error.getMessage().contains("./gradlew runAll"), error.getMessage());
    assertTrue(error.getMessage().contains("docker compose down -v && docker compose up -d --wait"), error.getMessage());
    assertTrue(error.getMessage().contains("rangiffler-auth"), error.getMessage());
  }

  private static void withProperties(String migrationCheck, String projectDir, Runnable action) {
    String previousCheck = System.getProperty("rfr.migrationCheck");
    String previousDir = System.getProperty("rfr.projectDir");
    System.setProperty("rfr.migrationCheck", migrationCheck);
    if (projectDir == null) {
      System.clearProperty("rfr.projectDir");
    } else {
      System.setProperty("rfr.projectDir", projectDir);
    }
    try {
      action.run();
    } finally {
      restore("rfr.migrationCheck", previousCheck);
      restore("rfr.projectDir", previousDir);
    }
  }

  private static void restore(String key, String previous) {
    if (previous == null) {
      System.clearProperty(key);
    } else {
      System.setProperty(key, previous);
    }
  }
}
