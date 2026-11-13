package io.student.rangiffler.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TearDown {
  private final List<Runnable> steps = new ArrayList<>();

  public TearDown with(Runnable... actions) {
    for (Runnable action : actions) {
      steps.add(action);
    }
    return this;
  }

  public <T> TearDown withEach(Iterable<T> items, Consumer<T> action) {
    for (T item : items) {
      steps.add(() -> action.accept(item));
    }
    return this;
  }

  public void run() {
    List<RuntimeException> failures = new ArrayList<>();
    for (Runnable step : steps) {
      try {
        step.run();
      } catch (RuntimeException failure) {
        failures.add(failure);
      }
    }
    if (!failures.isEmpty()) {
      RuntimeException first = failures.getFirst();
      failures.stream().skip(1).forEach(first::addSuppressed);
      throw first;
    }
  }
}
