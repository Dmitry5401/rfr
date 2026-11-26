package io.student.rangiffler.data;

import java.util.concurrent.atomic.AtomicReference;

public final class StaticUserFixture implements UserFixture {
  private final AtomicReference<TestUser> taken = new AtomicReference<>();

  @Override
  public TestUser user() {
    return taken.get();
  }

  @Override
  public void create() {
    TestUser user = UsersQueue.take();
    if (!taken.compareAndSet(null, user)) {
      UsersQueue.add(user);
      throw new IllegalStateException("Static user fixture already holds " + taken.get().username());
    }
  }

  @Override
  public void close() {
    TestUser user = taken.getAndSet(null);
    if (user != null) {
      UsersQueue.add(user);
    }
  }
}
