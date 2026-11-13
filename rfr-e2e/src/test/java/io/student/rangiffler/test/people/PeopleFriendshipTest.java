package io.student.rangiffler.test.people;

import static io.student.rangiffler.assertions.FriendshipSqlAssertions.assertFriendsCount;
import static io.student.rangiffler.assertions.FriendshipSqlAssertions.assertIncomeInvitationsCount;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright
@DisplayName("People: друзья и заявки")
class PeopleFriendshipTest {
  @Test
  @DisplayName("SQL-прекондишен friends создаёт ACCEPTED-дружбы")
  void friendsPreconditionIsCreated(Page page, @User(friends = 2) TestUser user) {
    assertFriendsCount(user.username(), 2);
    assertIncomeInvitationsCount(user.username(), 0);

    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openFriendsTab()
        .checkThatUsersCount(2);
  }

  @Test
  @DisplayName("SQL-прекондишен incomeInvitations создаёт PENDING-заявки")
  void incomeInvitationsPreconditionIsCreated(Page page, @User(incomeInvitations = 2) TestUser user) {
    assertIncomeInvitationsCount(user.username(), 2);
    assertFriendsCount(user.username(), 0);

    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openIncomeInvitationsTab()
        .checkThatUsersCount(2);
  }

  @Test
  @DisplayName("Заявка из UI удаляется при закрытии Store")
  void uiInvitationIsRemovedWhenStoreCloses(
      Page page,
      @User TestUser user,
      @User TestUser stranger) {
    assertFriendsCount(user.username(), 0);
    assertIncomeInvitationsCount(user.username(), 0);

    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openAllPeopleTab()
        .searchPeople(stranger.username())
        .clickAddForUser(stranger.username())
        .checkThatUserVisible(stranger.username());

    assertIncomeInvitationsCount(stranger.username(), 1);
  }
}
