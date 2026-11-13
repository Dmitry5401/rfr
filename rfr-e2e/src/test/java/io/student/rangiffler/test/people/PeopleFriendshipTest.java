package io.student.rangiffler.test.people;

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
  @DisplayName("Прекондишен friends виден во вкладке Friends")
  void friendsPreconditionIsCreated(Page page, @User(friends = 2) TestUser user) {
    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openFriendsTab()
        .checkThatUsersCount(2);
  }

  @Test
  @DisplayName("Прекондишен incomeInvitations виден во вкладке Income invitations")
  void incomeInvitationsPreconditionIsCreated(Page page, @User(incomeInvitations = 2) TestUser user) {
    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openIncomeInvitationsTab()
        .checkThatUsersCount(2);
  }

  @Test
  @DisplayName("Заявка из UI создаётся для пустого user и чистится Store при close")
  void uiInvitationIsRemovedWhenStoreCloses(
      Page page,
      @User TestUser user,
      @User TestUser stranger) {
    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openAllPeopleTab()
        .searchPeople(stranger.username())
        .clickAddForUser(stranger.username())
        .checkThatUserVisible(stranger.username());
  }
}
