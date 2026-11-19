package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

class ParallelExecutionTest {

  @Test
  void sixTestsUseThePoolFromProperties() throws IOException {
    int parallelism = Integer.parseInt(platformProperties().getProperty(
        "junit.jupiter.execution.parallel.config.fixed.parallelism"));
    SixHolds.IN_FLIGHT.set(0);
    SixHolds.MAX_IN_FLIGHT.set(0);
    SixHolds.THREADS.clear();

    EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
        .enableImplicitConfigurationParameters(true)
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(SixHolds.class))
        .execute();

    assertEquals(0, results.testEvents().failed().count(), "holder tests failed");
    assertEquals(6, results.testEvents().succeeded().count(), "holder tests did not all run");
    assertEquals(parallelism, SixHolds.MAX_IN_FLIGHT.get(), "peak in-flight tests");
    assertEquals(parallelism, SixHolds.THREADS.size(), "worker threads: " + SixHolds.THREADS);
  }

  private static Properties platformProperties() throws IOException {
    try (InputStream in = ParallelExecutionTest.class.getResourceAsStream("/junit-platform.properties")) {
      Properties properties = new Properties();
      properties.load(in);
      return properties;
    }
  }

  @ProbeTarget
  static class SixHolds {

    static final AtomicInteger IN_FLIGHT = new AtomicInteger();
    static final AtomicInteger MAX_IN_FLIGHT = new AtomicInteger();
    static final Set<String> THREADS = ConcurrentHashMap.newKeySet();

    @Test
    void first() {
      hold();
    }

    @Test
    void second() {
      hold();
    }

    @Test
    void third() {
      hold();
    }

    @Test
    void fourth() {
      hold();
    }

    @Test
    void fifth() {
      hold();
    }

    @Test
    void sixth() {
      hold();
    }

    private static void hold() {
      THREADS.add(Thread.currentThread().getName());
      int now = IN_FLIGHT.incrementAndGet();
      MAX_IN_FLIGHT.accumulateAndGet(now, Math::max);
      try {
        Thread.sleep(500);
      } catch (InterruptedException error) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(error);
      } finally {
        IN_FLIGHT.decrementAndGet();
      }
    }
  }
}
