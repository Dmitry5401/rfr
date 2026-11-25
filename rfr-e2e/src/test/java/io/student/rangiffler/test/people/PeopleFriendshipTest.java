package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeopleTab;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@UiTest
@ExtendWith(UserExtension.class)
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
        .table()
        .checkThatPeople(usernames(user.friends()));
  }

  @Test
  @DisplayName("Прекондишен incomeInvitations виден во вкладке Income invitations")
  void incomeInvitationsPreconditionIsCreated(Page page, @User(incomeInvitations = 2) TestUser user) {
    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .openTab(PeopleTab.INCOME_INVITATIONS)
        .table()
        .checkThatPeople(usernames(user.incomeInvitations()));
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
        .openTab(PeopleTab.ALL_PEOPLE)
        .searchPeople(stranger.username())
        .table()
        .row(stranger.username())
        .clickAdd()
        .checkThatWaiting();
  }

  private List<String> usernames(List<TestUser> users) {
    return users.stream().map(TestUser::username).toList();
  }
}
