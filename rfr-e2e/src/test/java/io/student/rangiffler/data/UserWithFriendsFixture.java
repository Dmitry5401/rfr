package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.ApiProfile;
import java.util.ArrayList;
import java.util.List;

public final class UserWithFriendsFixture implements UserFixture {
  private final UserFixture origin;
  private final int friendsCount;
  private final List<UserFixture> friends = new ArrayList<>();

  public UserWithFriendsFixture(UserFixture delegate, int friendsCount) {
    this.origin = delegate;
    this.friendsCount = friendsCount;
  }

  @Override
  public TestUser user() {
    return origin.user();
  }

  @Override
  public void create() {
    origin.create();
    ApiProfile me = new ApiProfile(user().username());
    List<Friendship> links = new ArrayList<>();
    for (int i = 0; i < friendsCount; i++) {
      UserFixture friend = new SingleUserFixture(TestUser.unique());
      friend.create();
      friends.add(friend);
      links.add(new Friendship(me, new ApiProfile(friend.user().username()), FriendshipStatus.ACCEPTED));
    }
    new Friendships(me, List.copyOf(links)).create();
  }

  @Override
  public void close() {
    new TearDown()
        .with(new Friendships(new ApiProfile(user().username()))::delete)
        .withEach(friends, UserFixture::close)
        .with(origin::close)
        .run();
  }
}
