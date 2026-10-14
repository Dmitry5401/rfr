package io.student.rangiffler.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class StaticUserFixture implements UserFixture {
  private static final BlockingQueue<TestUser> POOL = new LinkedBlockingQueue<>(List.of(
      new TestUser("duck", "12345"),
      new TestUser("bee", "12345"),
      new TestUser("owl", "12345")));
  private final BlockingQueue<TestUser> queue;
  private final Duration timeout;
  private final List<TestUser> taken = new ArrayList<>(1);

  public StaticUserFixture() {
    this(POOL, Duration.ofSeconds(30));
  }

  public StaticUserFixture(BlockingQueue<TestUser> queue, Duration timeout) {
    this.queue = queue;
    this.timeout = timeout;
  }

  @Override
  public TestUser user() {
    if (taken.isEmpty()) {
      throw new IllegalStateException("No static user was taken from the queue");
    }
    return taken.getFirst();
  }

  @Override
  public void create() {
    TestUser user;
    try {
      user = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException interruption) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for a static user", interruption);
    }
    if (user == null) {
      throw new IllegalStateException("No static user became free within " + timeout);
    }
    taken.add(user);
  }

  @Override
  public void close() {
    taken.forEach(queue::offer);
    taken.clear();
  }
}
