package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendships;
import java.util.ArrayList;
import java.util.List;

public final class UserWithFriendsFixture implements UserFixture {
  private final UserFixture delegate;
  private final int friendsCount;
  private final List<UserFixture> friends = new ArrayList<>();

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
    Friendships friendships = new Friendships(user().username());
    for (int i = 0; i < friendsCount; i++) {
      UserFixture friend = new SingleUserFixture(TestUser.unique());
      friend.create();
      friends.add(friend);
      friendships.create(friend.user().username());
    }
  }

  @Override
  public void close() {
    new TearDown()
        .with(new Friendships(user().username())::delete)
        .withEach(friends, UserFixture::close)
        .with(delegate::close)
        .run();
  }
}
