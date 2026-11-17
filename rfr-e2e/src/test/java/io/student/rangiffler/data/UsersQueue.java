package io.student.rangiffler.data;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class UsersQueue {
  private static final BlockingQueue<TestUser> USERS = new LinkedBlockingQueue<>();
  private static final Duration TIMEOUT = Duration.ofSeconds(2);

  private UsersQueue() {
  }

  public static void add(TestUser user) {
    USERS.add(user);
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

  public static int size() {
    return USERS.size();
  }

  public static void clear() {
    USERS.clear();
  }
}
