package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeoplePage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
@UsePlaywright
@DisplayName("People: вкладка Outcome invitations")
class OutcomeInvitationsTabTest {
  @Test
  @DisplayName("Вкладка Outcome invitations показывает приглашённого, Waiting... и Cancel, без друга")
  void outcomeTabShowsTheInviteeOnly(Page page, @User(friends = 1, outcomeInvitations = 1) TestUser user) {
    TestUser friend = user.friends().getFirst();
    TestUser invitee = user.outcomeInvitations().getFirst();

    openPeople(page, user)
        .openOutcomeInvitationsTab()
        .checkThatPeople(List.of(invitee.username()))
        .checkThatUserAbsent(friend.username())
        .checkThatWaitingAndCancelVisible(invitee.username());
  }

  @Test
  @DisplayName("Пустая вкладка Outcome invitations сообщает, что пользователей нет")
  void outcomeTabIsEmpty(Page page, @User TestUser user) {
    openPeople(page, user)
        .openOutcomeInvitationsTab()
        .checkThatNoUsers();
  }

  private PeoplePage openPeople(Page page, TestUser user) {
    return new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople();
  }
}
