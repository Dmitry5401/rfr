package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import io.student.rangiffler.jupiter.extension.SuiteExtension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

@Isolated
@ResourceLock("schema-migrations")
@Execution(ExecutionMode.SAME_THREAD)
@Tag("engine-probe")
@DisplayName("AllureEnvironmentExtension")
class AllureEnvironmentExtensionTest {

  @Test
  @DisplayName("После прогона во временном allure-results есть environment.properties со всеми ключами")
  void afterRunTemporaryResultsContainEnvironmentProperties() throws IOException {
    Path results = Files.createTempDirectory("allure-results");
    String previousResults = System.getProperty(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY);
    String previousCommit = System.getProperty("rfr.commit");
    System.setProperty(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY, results.toString());
    System.setProperty("rfr.commit", "abc1234");
    try {
      EngineExecutionResults execution = EngineTestKit.engine("junit-jupiter")
          .enableImplicitConfigurationParameters(true)
          .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
          .configurationParameter(
              "junit.jupiter.extensions.store.close.autocloseable.enabled", "true")
          .selectors(selectClass(EnvironmentProbe.class))
          .execute();

      assertEquals(1, execution.testEvents().succeeded().count(), "probe test did not succeed");

      Path environment = results.resolve(AllureEnvironmentExtension.ENVIRONMENT_FILE);
      assertTrue(Files.isRegularFile(environment), "missing " + environment);

      Properties properties = new Properties();
      try (InputStream in = Files.newInputStream(environment)) {
        properties.load(in);
      }
      for (String key : expectedKeys()) {
        assertTrue(properties.containsKey(key), "missing key " + key + " in " + properties);
      }
      assertEquals("abc1234", properties.getProperty("commit"));
    } finally {
      restore(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY, previousResults);
      restore("rfr.commit", previousCommit);
    }
  }

  @Test
  @DisplayName("JUnit не вызывает beforeAll следующих расширений после падения предыдущего")
  void laterBeforeAllIsSkippedWhenEarlierFails() {
    EarlierFails.BEFORE_ALLS.set(0);
    LaterObserves.BEFORE_ALLS.set(0);

    EngineTestKit.engine("junit-jupiter")
        .enableImplicitConfigurationParameters(true)
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(OrderedFailureProbe.class))
        .execute();

    assertEquals(1, EarlierFails.BEFORE_ALLS.get(), "earlier beforeAll did not run");
    assertEquals(0, LaterObserves.BEFORE_ALLS.get(),
        "JUnit still called beforeAll of a later extension after an earlier one failed");
  }

  @Test
  @DisplayName("environment.properties пишется, даже если beforeSuite другого расширения упал")
  void environmentIsWrittenWhenAnotherBeforeSuiteFails() throws IOException {
    Path results = Files.createTempDirectory("allure-results-fail");
    String previousResults = System.getProperty(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY);
    String previousCommit = System.getProperty("rfr.commit");
    System.setProperty(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY, results.toString());
    System.setProperty("rfr.commit", "deadbee");
    FailingBeforeSuite.THREW.set(false);
    try {
      EngineTestKit.engine("junit-jupiter")
          .enableImplicitConfigurationParameters(true)
          .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
          .configurationParameter(
              "junit.jupiter.extensions.store.close.autocloseable.enabled", "true")
          .selectors(selectClass(EnvironmentDespiteFailureProbe.class))
          .execute();

      assertTrue(FailingBeforeSuite.THREW.get(), "failing beforeSuite did not run");
      Path environment = results.resolve(AllureEnvironmentExtension.ENVIRONMENT_FILE);
      assertTrue(Files.isRegularFile(environment), "environment.properties was not written");
      String body = Files.readString(environment);
      assertTrue(body.contains("commit=deadbee"), body);
      assertTrue(body.contains("frontUrl="), body);
    } finally {
      restore(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY, previousResults);
      restore("rfr.commit", previousCommit);
    }
  }

  @Test
  @DisplayName("AllureEnvironmentExtension стоит в services раньше readiness и schema")
  void listedFirstInServicesFile() throws IOException {
    String listing;
    try (InputStream in = getClass().getResourceAsStream(
        "/META-INF/services/org.junit.jupiter.api.extension.Extension")) {
      listing = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    int environment = listing.indexOf(AllureEnvironmentExtension.class.getName());
    int readiness = listing.indexOf(StandReadinessExtension.class.getName());
    int schema = listing.indexOf(StandSchemaExtension.class.getName());
    assertTrue(environment >= 0, listing);
    assertTrue(readiness > environment, listing);
    assertTrue(schema > readiness, listing);
  }

  @Test
  @DisplayName("Недоступный MySQL даёт unavailable, файл всё равно пишется")
  void unavailableMysqlWritesUnavailableVersions() throws IOException {
    Path results = Files.createTempDirectory("allure-results-mysql");
    String previousDb = System.getProperty("rfr.dbUrl");
    System.setProperty("rfr.dbUrl", "jdbc:mysql://127.0.0.1:1");
    for (Databases database : Databases.values()) {
      database.close();
    }
    try {
      Map<String, String> values = new AllureEnvironmentExtension().environment();
      AllureEnvironmentExtension.write(results, values);
      Properties properties = new Properties();
      try (InputStream in = Files.newInputStream(results.resolve(AllureEnvironmentExtension.ENVIRONMENT_FILE))) {
        properties.load(in);
      }
      for (Databases database : Databases.values()) {
        assertEquals("unavailable", properties.getProperty(database.schema()), database.schema());
      }
      assertFalse(properties.isEmpty());
    } finally {
      for (Databases database : Databases.values()) {
        database.close();
      }
      restore("rfr.dbUrl", previousDb);
    }
  }

  private static List<String> expectedKeys() {
    Map<String, String> keys = new LinkedHashMap<>();
    keys.put("frontUrl", "");
    keys.put("authUrl", "");
    keys.put("apiUrl", "");
    keys.put("browser", "");
    keys.put("headless", "");
    keys.put("commit", "");
    Arrays.stream(Databases.values()).forEach(database -> keys.put(database.schema(), ""));
    return List.copyOf(keys.keySet());
  }

  private static void restore(String key, String previous) {
    if (previous == null) {
      System.clearProperty(key);
    } else {
      System.setProperty(key, previous);
    }
  }

  static final class EnvironmentWriter implements SuiteExtension {
    @Override
    public void afterSuite(ExtensionContext context) {
      new AllureEnvironmentExtension().afterSuite(context);
    }
  }

  static final class FailingBeforeSuite implements SuiteExtension {
    static final AtomicBoolean THREW = new AtomicBoolean();

    @Override
    public void beforeSuite(ExtensionContext context) {
      THREW.set(true);
      throw new IllegalStateException("stand is down");
    }
  }

  static final class EarlierFails implements BeforeAllCallback {
    static final AtomicInteger BEFORE_ALLS = new AtomicInteger();

    @Override
    public void beforeAll(ExtensionContext context) {
      BEFORE_ALLS.incrementAndGet();
      throw new IllegalStateException("earlier beforeAll failed");
    }
  }

  static final class LaterObserves implements BeforeAllCallback {
    static final AtomicInteger BEFORE_ALLS = new AtomicInteger();

    @Override
    public void beforeAll(ExtensionContext context) {
      BEFORE_ALLS.incrementAndGet();
      context.getRoot().getStore(Namespace.GLOBAL).put(getClass(), "observed");
    }
  }

  @ProbeTarget
  @ExtendWith(EnvironmentWriter.class)
  static final class EnvironmentProbe {
    @Test
    void runs() {
    }
  }

  @ProbeTarget
  @ExtendWith({EnvironmentWriter.class, FailingBeforeSuite.class})
  static final class EnvironmentDespiteFailureProbe {
    @Test
    void neverRuns() {
    }
  }

  @ProbeTarget
  @ExtendWith({EarlierFails.class, LaterObserves.class})
  static final class OrderedFailureProbe {
    @Test
    void neverRuns() {
    }
  }
}
