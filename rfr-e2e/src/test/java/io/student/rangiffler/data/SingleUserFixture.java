package io.student.rangiffler.data;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Invitations;
import io.student.rangiffler.data.db.Profile;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public final class SingleUserFixture implements UserFixture {
  private final TestUser user;
  private final AuthAccount authAccount;
  private final Profile profile;
  private final Friendships friendships;
  private final Invitations invitations;

  public SingleUserFixture(TestUser user) {
    this.user = user;
    this.authAccount = new AuthAccount(user.username());
    this.profile = new Profile(user.username());
    this.friendships = new Friendships(user.username());
    this.invitations = new Invitations(user.username());
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void create() {
    String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.password());
    authAccount.create(encoded);
    profile.create();
  }

  @Override
  public void close() {
    new TearDown()
        .with(invitations::delete, friendships::delete, profile::delete, authAccount::delete)
        .run();
  }
}
