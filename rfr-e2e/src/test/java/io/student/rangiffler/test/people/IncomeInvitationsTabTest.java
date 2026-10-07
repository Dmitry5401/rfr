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
@DisplayName("Вкладка Income invitations")
class IncomeInvitationsTabTest {
  @Test
  @DisplayName("Показывает отправителей приглашений и не показывает друзей и приглашённых")
  void listsOnlyInviters(Page page,
                         @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
    PeopleLogin.open(page, user)
        .clickIncomeInvitations()
        .checkThatPeople(List.of(user.incomeInvitations().getFirst().username()));
  }

  @Test
  @DisplayName("У приглашения кнопки Accept и Decline")
  void invitationOffersAcceptAndDecline(Page page, @User(incomeInvitations = 1) TestUser user) {
    PeopleLogin.open(page, user)
        .clickIncomeInvitations()
        .checkThatActionsOf(user.incomeInvitations().getFirst().username(), "Accept", "Decline");
  }

  @Test
  @DisplayName("Без входящих приглашений показывает пустое состояние")
  void showsEmptyStateWithoutInvitations(Page page, @User TestUser user) {
    PeopleLogin.open(page, user)
        .clickIncomeInvitations()
        .checkThatNoUsers();
  }
}
