package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.SuiteExtension;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/** Probes {@link SuiteExtension} through {@link EngineTestKit}. */
class SuiteExtensionTest {

  @Test
  void beforeSuiteRunsInsideEngineTestKit() {
    RecordingSuiteExtension.CALLS.set(0);

    EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
        .enableImplicitConfigurationParameters(true)
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(Observed.class))
        .execute();

    assertEquals(1, results.testEvents().succeeded().count(), "probe test did not succeed");
    assertEquals(1, RecordingSuiteExtension.CALLS.get(), "SuiteExtension.beforeSuite did not run");
  }

  static final class RecordingSuiteExtension implements SuiteExtension {
    static final AtomicInteger CALLS = new AtomicInteger();

    @Override
    public void beforeSuite(ExtensionContext context) {
      CALLS.incrementAndGet();
    }
  }

  @ProbeTarget
  @ExtendWith(RecordingSuiteExtension.class)
  static class Observed {
    @Test
    void runs() {
    }
  }
}
