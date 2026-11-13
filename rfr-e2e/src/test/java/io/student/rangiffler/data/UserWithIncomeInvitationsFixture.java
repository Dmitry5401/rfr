package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbc;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class UserWithIncomeInvitationsFixture implements UserFixture {
  private final UserFixture delegate;
  private final int invitationsCount;
  private final List<UserFixture> requesters = new ArrayList<>();
  private final JdbcClient api = Jdbc.client(Databases.API);

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
    for (int i = 0; i < invitationsCount; i++) {
      UserFixture requester = new SingleUserFixture(TestUser.unique());
      requester.create();
      requesters.add(requester);
      requireOneRow(api.sql(Jdbc.sql("api/create-invitation.sql"))
          .param("requester", requester.user().username())
          .param("addressee", user().username())
          .update(), "invitation " + requester.user().username() + " -> " + user().username());
    }
  }

  @Override
  public void close() {
    new TearDown()
        .with(this::deleteInvitations)
        .withEach(requesters, UserFixture::close)
        .with(delegate::close)
        .run();
  }

  private void deleteInvitations() {
    api.sql(Jdbc.sql("api/delete-invitations.sql")).param("username", user().username()).update();
  }

  private static void requireOneRow(int updated, String what) {
    if (updated != 1) {
      throw new IllegalStateException("Expected one inserted row for " + what + ", got " + updated);
    }
  }
}
