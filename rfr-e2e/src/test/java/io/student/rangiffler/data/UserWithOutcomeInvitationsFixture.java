package io.student.rangiffler.data;

import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import java.util.ArrayList;
import java.util.List;

public final class UserWithOutcomeInvitationsFixture implements UserFixture {
  private final UserFixture origin;
  private final int invitationsCount;
  private final List<UserFixture> invitees = new ArrayList<>();

  public UserWithOutcomeInvitationsFixture(UserFixture delegate, int invitationsCount) {
    this.origin = delegate;
    this.invitationsCount = invitationsCount;
  }

  @Override
  public TestUser user() {
    return origin.user().withOutcomeInvitations(invitees.stream().map(UserFixture::user).toList());
  }

  @Override
  public void create() {
    origin.create();
    ApiProfile me = new ApiProfile(user().username());
    for (int i = 0; i < invitationsCount; i++) {
      UserFixture invitee = new SingleUserFixture(TestUser.unique());
      invitee.create();
      invitees.add(invitee);
      new Friendship(me, new ApiProfile(invitee.user().username()), FriendshipStatus.PENDING).create();
    }
  }

  @Override
  public void close() {
    ApiProfile me = new ApiProfile(user().username());
    new TearDown()
        .withEach(invitees, invitee -> new Friendship(
            me,
            new ApiProfile(invitee.user().username()),
            FriendshipStatus.PENDING).delete())
        .withEach(invitees, UserFixture::close)
        .with(origin::close)
        .run();
  }
}
