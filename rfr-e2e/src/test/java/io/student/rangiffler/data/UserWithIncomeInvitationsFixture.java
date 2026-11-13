package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Invitations;
import io.student.rangiffler.data.db.Profile;
import java.util.ArrayList;
import java.util.List;

public final class UserWithIncomeInvitationsFixture implements UserFixture {
  private final UserFixture delegate;
  private final int invitationsCount;
  private final List<UserFixture> requesters = new ArrayList<>();

  public UserWithIncomeInvitationsFixture(UserFixture delegate, int invitationsCount) {
    this.delegate = delegate;
    this.invitationsCount = invitationsCount;
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
    for (int i = 0; i < invitationsCount; i++) {
      UserFixture requester = new SingleUserFixture(TestUser.unique());
      requester.create();
      requesters.add(requester);
      links.add(new Friendship(
          new Profile(requester.user().username()),
          me,
          FriendshipStatus.PENDING));
    }
    new Invitations(me, List.copyOf(links)).create();
  }

  @Override
  public void close() {
    new TearDown()
        .with(new Invitations(new Profile(user().username()))::delete)
        .withEach(requesters, UserFixture::close)
        .with(delegate::close)
        .run();
  }
}
