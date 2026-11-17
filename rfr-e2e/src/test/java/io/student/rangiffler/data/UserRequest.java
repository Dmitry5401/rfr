package io.student.rangiffler.data;

import io.student.rangiffler.jupiter.annotation.User;

public record UserRequest(int friends, int incomeInvitations, int outcomeInvitations) {
  public UserRequest {
    if (friends < 0) {
      throw new IllegalArgumentException("friends must be >= 0, got " + friends);
    }
    if (incomeInvitations < 0) {
      throw new IllegalArgumentException("incomeInvitations must be >= 0, got " + incomeInvitations);
    }
    if (outcomeInvitations < 0) {
      throw new IllegalArgumentException("outcomeInvitations must be >= 0, got " + outcomeInvitations);
    }
  }

  public UserRequest(User user) {
    this(user.friends(), user.incomeInvitations(), user.outcomeInvitations());
  }

  public static UserRequest from(User user) {
    return new UserRequest(user);
  }

  public UserFixture fixture() {
    UserFixture fixture = new SingleUserFixture(TestUser.unique());
    if (friends > 0) {
      fixture = new UserWithFriendsFixture(fixture, friends);
    }
    if (incomeInvitations > 0) {
      fixture = new UserWithIncomeInvitationsFixture(fixture, incomeInvitations);
    }
    if (outcomeInvitations > 0) {
      fixture = new UserWithOutcomeInvitationsFixture(fixture, outcomeInvitations);
    }
    return fixture;
  }
}
