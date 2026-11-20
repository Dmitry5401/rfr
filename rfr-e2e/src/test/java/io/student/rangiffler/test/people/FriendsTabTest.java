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
@DisplayName("People: вкладка Friends")
class FriendsTabTest {
  @Test
  @DisplayName("Вкладка Friends показывает друга и кнопку Remove, без входящей заявки")
  void friendsTabShowsTheFriendOnly(Page page, @User(friends = 1, incomeInvitations = 1) TestUser user) {
    TestUser friend = user.friends().getFirst();
    TestUser inviter = user.incomeInvitations().getFirst();

    openPeople(page, user)
        .openFriendsTab()
        .checkThatPeople(List.of(friend.username()))
        .checkThatUserAbsent(inviter.username())
        .checkThatRemoveButtonVisible(friend.username());
  }

  @Test
  @DisplayName("Пустая вкладка Friends сообщает, что пользователей нет")
  void friendsTabIsEmpty(Page page, @User TestUser user) {
    openPeople(page, user)
        .openFriendsTab()
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
