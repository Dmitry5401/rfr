package io.student.rangiffler.jupiter;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.jupiter.annotation.User;

public record UserRequest(int friends, int incomeInvitations) {
  public static UserRequest from(User annotation) {
    return new UserRequest(annotation.friends(), annotation.incomeInvitations());
  }

  public UserFixture fixture() {
    return new UserFixture(TestUser.unique());
  }
}
