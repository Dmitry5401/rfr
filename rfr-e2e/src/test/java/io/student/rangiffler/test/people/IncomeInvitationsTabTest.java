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
@DisplayName("People: вкладка Income invitations")
class IncomeInvitationsTabTest {
  @Test
  @DisplayName("Вкладка Income invitations показывает заявителя и кнопки Accept и Decline, без друга")
  void incomeTabShowsTheInviterOnly(Page page, @User(friends = 1, incomeInvitations = 1) TestUser user) {
    TestUser inviter = user.incomeInvitations().getFirst();

    openPeople(page, user)
        .openTab(PeopleTab.INCOME_INVITATIONS)
        .table()
        .checkThatPeople(List.of(inviter.username()))
        .row(inviter.username())
        .checkThatActions("Accept", "Decline");
  }

  @Test
  @DisplayName("Пустая вкладка Income invitations сообщает, что пользователей нет")
  void incomeTabIsEmpty(Page page, @User TestUser user) {
    openPeople(page, user)
        .openTab(PeopleTab.INCOME_INVITATIONS)
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
