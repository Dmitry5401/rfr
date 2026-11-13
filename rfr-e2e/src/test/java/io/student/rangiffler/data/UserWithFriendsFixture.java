package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Profile;
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
    Profile me = new Profile(user().username());
    List<Friendship> links = new ArrayList<>();
    for (int i = 0; i < friendsCount; i++) {
      UserFixture friend = new SingleUserFixture(TestUser.unique());
      friend.create();
      friends.add(friend);
      links.add(new Friendship(me, new Profile(friend.user().username()), FriendshipStatus.ACCEPTED));
    }
    new Friendships(me, List.copyOf(links)).create();
  }

  @Override
  public void close() {
    new TearDown()
        .with(new Friendships(new Profile(user().username()))::delete)
        .withEach(friends, UserFixture::close)
        .with(delegate::close)
        .run();
  }
}
