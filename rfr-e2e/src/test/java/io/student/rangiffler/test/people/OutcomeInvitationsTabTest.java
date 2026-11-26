package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.PeopleTab;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@UiTest
@ExtendWith(UserExtension.class)
@DisplayName("People: вкладка Outcome invitations")
class OutcomeInvitationsTabTest {
  @Test
  @DisplayName("Вкладка Outcome invitations показывает приглашённого, Waiting... и Cancel, без друга")
  void outcomeTabShowsTheInviteeOnly(Page page, @User(friends = 1, outcomeInvitations = 1) TestUser user) {
    TestUser invitee = user.outcomeInvitations().getFirst();

    openPeople(page, user)
        .openTab(PeopleTab.OUTCOME_INVITATIONS)
        .table()
        .checkThatPeople(List.of(invitee.username()))
        .row(invitee.username())
        .checkThatWaiting()
        .checkThatActions("Cancel");
  }

  @Test
  @DisplayName("Пустая вкладка Outcome invitations сообщает, что пользователей нет")
  void outcomeTabIsEmpty(Page page, @User TestUser user) {
    openPeople(page, user)
        .openTab(PeopleTab.OUTCOME_INVITATIONS)
        .table()
        .checkThatEmpty();
  }

  private PeoplePage openPeople(Page page, TestUser user) {
    return new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople();
  }
}
