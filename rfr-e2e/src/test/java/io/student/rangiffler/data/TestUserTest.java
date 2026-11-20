package io.student.rangiffler.data;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUserTest {

  @Test
  void uniqueUserHasNoRelations() {
    TestUser user = TestUser.unique();

    assertTrue(user.friends().isEmpty());
    assertTrue(user.incomeInvitations().isEmpty());
    assertTrue(user.outcomeInvitations().isEmpty());
  }

  @Test
  void relationsStayWhenTheOtherListChanges() {
    TestUser friend = TestUser.unique();
    TestUser inviter = TestUser.unique();
    TestUser invitee = TestUser.unique();
    TestUser user = TestUser.unique().withFriends(List.of(friend));

    TestUser withInvitation = user.withIncomeInvitations(List.of(inviter));
    TestUser withOutcome = withInvitation.withOutcomeInvitations(List.of(invitee));

    assertEquals(List.of(friend), withOutcome.friends());
    assertEquals(List.of(inviter), withOutcome.incomeInvitations());
    assertEquals(List.of(invitee), withOutcome.outcomeInvitations());
    assertTrue(user.incomeInvitations().isEmpty());
    assertTrue(withInvitation.outcomeInvitations().isEmpty());
  }

  @Test
  void storedListsDoNotFollowLaterChangesOfTheSource() {
    TestUser friend = TestUser.unique();
    List<TestUser> source = new ArrayList<>();
    source.add(friend);
    TestUser user = TestUser.unique().withFriends(source);

    source.clear();

    assertEquals(List.of(friend), user.friends());
    assertThrows(UnsupportedOperationException.class, () -> user.friends().clear());
  }

  @Test
  void toStringHidesThePassword() {
    TestUser user = new TestUser("alice", "secret");

    assertFalse(user.toString().contains("secret"));
    assertTrue(user.toString().contains("alice"));
    assertTrue(user.toString().contains("friends="));
    assertTrue(user.toString().contains("incomeInvitations="));
    assertTrue(user.toString().contains("outcomeInvitations="));
  }
}
