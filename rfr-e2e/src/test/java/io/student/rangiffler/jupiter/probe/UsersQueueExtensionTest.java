package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UsersQueue;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("UsersQueueExtension")
class UsersQueueExtensionTest {

  @Test
  @DisplayName("Четыре теста подряд на пуле из трёх зелёные")
  void fourTestsOnAPoolOfThreeSucceed() {
    EngineExecutionResults results = run(FourInARow.class);

    assertEquals(4, results.testEvents().succeeded().count(), "Four static-user tests did not succeed");
    assertEquals(UsersQueue.poolUsernames(), UsersQueue.usernames(), "Pool did not contain the same three users");
  }

  @Test
  @DisplayName("После упавшего теста пользователь вернулся в очередь")
  void userReturnsToTheQueueAfterAFailedTest() {
    EngineExecutionResults results = run(Failing.class);
    Map<String, String> entries = entriesOf(results);

    assertEquals(1, results.testEvents().failed().count(), "Failing static-user probe did not fail");
    assertEquals(UsersQueue.poolUsernames(), UsersQueue.usernames(), "Static user was not returned to the pool");
    assertTrue(UsersQueue.poolUsernames().contains(entries.get("username")),
        "Returned user is not one of the pool");
  }

  private EngineExecutionResults run(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .enableImplicitConfigurationParameters(true)
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(target))
        .execute();
  }

  private Map<String, String> entriesOf(EngineExecutionResults results) {
    Map<String, String> entries = new HashMap<>();
    results.allEvents().reportingEntryPublished().stream()
        .map(event -> event.getRequiredPayload(ReportEntry.class))
        .forEach(entry -> entries.putAll(entry.getKeyValuePairs()));
    return entries;
  }

  @ProbeTarget
  static final class FourInARow {
    @Test
    void first(@StaticUser TestUser user) {
      user.username();
    }

    @Test
    void second(@StaticUser TestUser user) {
      user.username();
    }

    @Test
    void third(@StaticUser TestUser user) {
      user.username();
    }

    @Test
    void fourth(@StaticUser TestUser user) {
      user.username();
    }
  }

  @ProbeTarget
  static final class Failing {
    @Test
    void fail(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
      throw new IllegalStateException("Probe target fails on purpose");
    }
  }
}
