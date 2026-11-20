package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Closes the SuiteExtension ServiceLoader-only rule: production implementations must not be
 * wired through {@code @ExtendWith} outside {@code jupiter/probe} {@code @ProbeTarget} stubs.
 */
@DisplayName("SuiteExtension registration")
class SuiteExtensionRegistrationTest {

  private static final List<String> SUITE_EXTENSIONS = List.of(
      AllureEnvironmentExtension.class.getSimpleName(),
      StandReadinessExtension.class.getSimpleName(),
      StandSchemaExtension.class.getSimpleName());

  @Test
  @DisplayName("Вне jupiter/probe нет @ExtendWith на реальных SuiteExtension")
  void productionSourcesDoNotExtendWithSuiteExtensions() throws IOException {
    Path testRoot = Path.of("src/test/java");
    Path probeRoot = testRoot.resolve("io/student/rangiffler/jupiter/probe").normalize();
    assertTrue(Files.isDirectory(testRoot), "expected to run with rfr-e2e project dir");
    try (Stream<Path> files = Files.walk(testRoot)) {
      List<String> offenders = files
          .filter(path -> path.getFileName() != null && path.getFileName().toString().endsWith(".java"))
          .filter(path -> !path.normalize().startsWith(probeRoot))
          .flatMap(path -> findExtendWithOffenders(path).stream())
          .toList();
      assertTrue(offenders.isEmpty(), String.join(System.lineSeparator(), offenders));
    }
  }

  private static List<String> findExtendWithOffenders(Path path) {
    try {
      String source = Files.readString(path);
      return SUITE_EXTENSIONS.stream()
          .filter(name -> source.contains("@ExtendWith(" + name + ".class)")
              || source.contains("@ExtendWith({" + name + ".class")
              || source.contains("@ExtendWith({ " + name + ".class")
              || source.matches("(?s).*@ExtendWith\\(\\{[^}]*\\b" + name + "\\.class[^}]*\\}\\).*")
          )
          .map(name -> path + " registers " + name + " via @ExtendWith")
          .toList();
    } catch (IOException failure) {
      return List.of(path + " could not be read: " + failure.getMessage());
    }
  }
}
