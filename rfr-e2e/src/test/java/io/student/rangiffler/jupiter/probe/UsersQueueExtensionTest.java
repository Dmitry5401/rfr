package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UsersQueue;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("UsersQueueExtension")
class UsersQueueExtensionTest {

  @Test
  @DisplayName("Четыре теста подряд на пуле из трёх зелёные")
  void fourTestsOnAPoolOfThreeSucceed() {
    UsersQueue.clear();
    List.of(TestUser.unique(), TestUser.unique(), TestUser.unique()).forEach(UsersQueue::add);
    try {
      EngineExecutionResults results = run(FourInARow.class);

      assertEquals(4, results.testEvents().succeeded().count(), "Four static-user tests did not succeed");
      assertEquals(3, UsersQueue.size(), "Pool did not contain three users after the tests");
    } finally {
      UsersQueue.clear();
    }
  }

  @Test
  @DisplayName("После упавшего теста пользователь вернулся в очередь")
  void userReturnsToTheQueueAfterAFailedTest() {
    UsersQueue.clear();
    TestUser only = TestUser.unique();
    UsersQueue.add(only);
    try {
      EngineExecutionResults results = run(Failing.class);

      assertEquals(1, results.testEvents().failed().count(), "Failing static-user probe did not fail");
      assertEquals(1, UsersQueue.size(), "Static user was not returned to the queue");
      assertEquals(only.username(), UsersQueue.take().username(), "Queue returned a different user");
    } finally {
      UsersQueue.clear();
    }
  }

  private EngineExecutionResults run(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(target))
        .execute();
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
    void fail(@StaticUser TestUser user) {
      throw new IllegalStateException("Probe target fails on purpose: " + user.username());
    }
  }
}
