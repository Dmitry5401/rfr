package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("UsersQueueExtension")
class UsersQueueExtensionTest {
  @Test
  @DisplayName("Четыре теста подряд на пуле из трёх зелёные")
  void fourTestsOnPoolOfThreeSucceed() {
    assertEquals(4L, run(FourInRow.class).testEvents().succeeded().count(),
        "Four tests in a row on a pool of three did not all succeed");
  }

  @Test
  @DisplayName("После упавшего теста пользователь вернулся в очередь")
  void userReturnsToQueueAfterFailedTest() {
    assertEquals(4L, run(FourFailing.class).testEvents().failed().stream()
            .map(event -> event.getRequiredPayload(TestExecutionResult.class))
            .flatMap(result -> result.getThrowable().stream())
            .filter(failure -> "Probe target fails on purpose".equals(failure.getMessage()))
            .count(),
        "Failed tests did not return their static users to the queue");
  }

  private EngineExecutionResults run(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(target))
        .execute();
  }

  @ProbeTarget
  static final class FourInRow {
    @Test
    void first(@StaticUser TestUser user) {
    }

    @Test
    void second(@StaticUser TestUser user) {
    }

    @Test
    void third(@StaticUser TestUser user) {
    }

    @Test
    void fourth(@StaticUser TestUser user) {
    }
  }

  @ProbeTarget
  static final class FourFailing {
    @Test
    void first(@StaticUser TestUser user) {
      throw new IllegalStateException("Probe target fails on purpose");
    }

    @Test
    void second(@StaticUser TestUser user) {
      throw new IllegalStateException("Probe target fails on purpose");
    }

    @Test
    void third(@StaticUser TestUser user) {
      throw new IllegalStateException("Probe target fails on purpose");
    }

    @Test
    void fourth(@StaticUser TestUser user) {
      throw new IllegalStateException("Probe target fails on purpose");
    }
  }
}
