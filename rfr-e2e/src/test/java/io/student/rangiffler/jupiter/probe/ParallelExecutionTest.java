package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class ParallelExecutionTest {

  private static final AtomicInteger IN_FLIGHT = new AtomicInteger();
  private static final AtomicInteger MAX_IN_FLIGHT = new AtomicInteger();
  private static final Set<String> THREADS = ConcurrentHashMap.newKeySet();

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

  @AfterAll
  static void threeThreadsAtOnce() {
    assertEquals(3, MAX_IN_FLIGHT.get(), "peak in-flight tests");
    assertEquals(3, THREADS.size(), "worker threads: " + THREADS);
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
