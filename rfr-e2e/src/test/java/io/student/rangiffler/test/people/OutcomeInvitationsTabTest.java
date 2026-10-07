package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
@UsePlaywright
@DisplayName("Вкладка Outcome invitations")
class OutcomeInvitationsTabTest {
  @Test
  @DisplayName("Показывает приглашённых и не показывает друзей и отправителей")
  void listsOnlyInvitees(Page page,
                         @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
    PeopleLogin.open(page, user)
        .clickOutcomeInvitations()
        .checkThatPeople(List.of(user.outcomeInvitations().getFirst().username()));
  }

  @Test
  @DisplayName("У исходящего приглашения статус Waiting и кнопка Cancel")
  void invitationIsWaitingAndCancellable(Page page, @User(outcomeInvitations = 1) TestUser user) {
    String invitee = user.outcomeInvitations().getFirst().username();
    PeopleLogin.open(page, user)
        .clickOutcomeInvitations()
        .checkThatInvitationSent(invitee)
        .checkThatActionsOf(invitee, "Cancel");
  }

  @Test
  @DisplayName("Без исходящих приглашений показывает пустое состояние")
  void showsEmptyStateWithoutInvitations(Page page, @User TestUser user) {
    PeopleLogin.open(page, user)
        .clickOutcomeInvitations()
        .checkThatNoUsers();
  }
}
