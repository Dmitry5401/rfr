package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.testkit.engine.EngineTestKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("AllureEnvironmentExtension")
class AllureEnvironmentExtensionTest {
  @Test
  @DisplayName("После прогона в каталоге результатов есть environment.properties со всеми ключами")
  void environmentHasAllKeys(@TempDir Path dir) throws IOException {
    Path results = dir.resolve("allure-results-4127");
    EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
        .configurationParameter("allure.results.directory", results.toString())
        .selectors(selectClass(Plain.class))
        .execute();
    Set<String> keys = Files.readAllLines(results.resolve("environment.properties")).stream()
        .map(line -> line.substring(0, line.indexOf('=')))
        .collect(Collectors.toSet());
    assertEquals(
        Set.of("frontUrl", "authUrl", "apiUrl", "browser", "headless", "commit", "rangiffler-auth", "rangiffler-api"),
        keys,
        "Allure environment does not hold exactly the expected keys");
  }

  @ProbeTarget
  static final class Plain {
    @Test
    void passes() {
    }
  }
}
