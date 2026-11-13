package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Invitations;
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
    Invitations invitations = new Invitations(user().username());
    for (int i = 0; i < invitationsCount; i++) {
      UserFixture requester = new SingleUserFixture(TestUser.unique());
      requester.create();
      requesters.add(requester);
      invitations.create(requester.user().username());
    }
  }

  @Override
  public void close() {
    new TearDown()
        .with(new Invitations(user().username())::delete)
        .withEach(requesters, UserFixture::close)
        .with(delegate::close)
        .run();
  }
}
