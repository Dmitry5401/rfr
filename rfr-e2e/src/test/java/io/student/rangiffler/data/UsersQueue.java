package io.student.rangiffler.data;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class UsersQueue {
  private static final BlockingQueue<TestUser> USERS = new LinkedBlockingQueue<>();
  private static final Duration TIMEOUT = Duration.ofSeconds(2);
  private static final List<TestUser> POOL = List.of(
      new TestUser("rfr_pool_a", "pool-a"),
      new TestUser("rfr_pool_b", "pool-b"),
      new TestUser("rfr_pool_c", "pool-c"));

  static {
    USERS.addAll(POOL);
  }

  private UsersQueue() {
  }

  public static TestUser take() {
    try {
      TestUser user = USERS.poll(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
      if (user == null) {
        throw new IllegalStateException(
            "No static user became available within " + TIMEOUT.toSeconds() + " seconds");
      }
      return user;
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for a static user", error);
    }
  }

  static void add(TestUser user) {
    USERS.add(user);
  }

  public static Set<String> usernames() {
    Set<String> names = new LinkedHashSet<>();
    for (TestUser user : USERS) {
      names.add(user.username());
    }
    return Set.copyOf(names);
  }

  public static Set<String> poolUsernames() {
    Set<String> names = new LinkedHashSet<>();
    for (TestUser user : POOL) {
      names.add(user.username());
    }
    return Set.copyOf(names);
  }
}
