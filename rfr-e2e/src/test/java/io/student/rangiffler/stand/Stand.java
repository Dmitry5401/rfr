package io.student.rangiffler.stand;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public final class Stand {
  private final List<Service> services;
  private final Duration timeout;
  private final Duration pause;

  public Stand(List<Service> services, Duration timeout, Duration pause) {
    this.services = services;
    this.timeout = timeout;
    this.pause = pause;
  }

  public void await() {
    long deadline = System.nanoTime() + timeout.toNanos();
    List<Service> silent = silent();
    while (!silent.isEmpty()) {
      if (System.nanoTime() - deadline >= 0) {
        throw new IllegalStateException(
            "Stand did not respond within " + timeout.toMillis() + " ms, start the silent services and rerun the tests"
                + System.lineSeparator()
                + silent.stream()
                    .map(service -> service.name() + ": " + service.hint())
                    .collect(Collectors.joining(System.lineSeparator())));
      }
      sleep();
      silent = silent();
    }
  }

  private List<Service> silent() {
    return services.stream().filter(service -> !service.responds()).toList();
  }

  private void sleep() {
    try {
      Thread.sleep(pause);
    } catch (InterruptedException failure) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Waiting for the stand was interrupted", failure);
    }
  }
}
