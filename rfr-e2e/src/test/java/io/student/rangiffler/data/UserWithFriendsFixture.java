package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbc;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class UserWithFriendsFixture implements UserFixture {
  private final UserFixture delegate;
  private final int friendsCount;
  private final List<UserFixture> friends = new ArrayList<>();
  private final JdbcClient api = Jdbc.client(Databases.API);

  public UserWithFriendsFixture(UserFixture delegate, int friendsCount) {
    this.delegate = delegate;
    this.friendsCount = friendsCount;
  }

  @Override
  public TestUser user() {
    return delegate.user();
  }

  @Override
  public void create() {
    delegate.create();
    for (int i = 0; i < friendsCount; i++) {
      UserFixture friend = new SingleUserFixture(TestUser.unique());
      friend.create();
      friends.add(friend);
      requireOneRow(api.sql(Jdbc.sql("api/create-friendship.sql"))
          .param("requester", user().username())
          .param("addressee", friend.user().username())
          .update(), "friendship " + user().username() + " -> " + friend.user().username());
    }
  }

  @Override
  public void close() {
    new TearDown()
        .with(this::deleteFriends)
        .withEach(friends, UserFixture::close)
        .with(delegate::close)
        .run();
  }

  private void deleteFriends() {
    api.sql(Jdbc.sql("api/delete-friends.sql")).param("username", user().username()).update();
  }

  private static void requireOneRow(int updated, String what) {
    if (updated != 1) {
      throw new IllegalStateException("Expected one inserted row for " + what + ", got " + updated);
    }
  }
}
