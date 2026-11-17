package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeoplePage {
  public static final String URL = TestConfig.frontUrl() + "/people";
  private final Page page;
  private final Locator heading;
  private final Locator friendsTab;
  private final Locator allPeopleTab;
  private final Locator incomeInvitationsTab;
  private final Locator outcomeInvitationsTab;
  private final Locator searchInput;
  private final Locator searchButton;
  private final Locator peopleRows;
  private final Locator emptyState;

  public PeoplePage(Page page) {
    this.page = page;
    this.heading = page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("People").setExact(true));
    this.friendsTab = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("Friends").setExact(true));
    this.allPeopleTab = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("All People").setExact(true));
    this.incomeInvitationsTab = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("Income invitations").setExact(true));
    this.outcomeInvitationsTab = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("Outcome invitations").setExact(true));
    this.searchInput = page.getByLabel("search people");
    this.searchButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("search").setExact(true));
    this.peopleRows = page.getByRole(AriaRole.TABPANEL).getByRole(AriaRole.ROW)
        .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.ROWHEADER)));
    this.emptyState = page.getByText("There are no users yet",
        new Page.GetByTextOptions().setExact(true));
  }

  @Step("Открыть вкладку Friends")
  public PeoplePage openFriendsTab() {
    friendsTab.click();
    return this;
  }

  @Step("Открыть вкладку All People")
  public PeoplePage openAllPeopleTab() {
    allPeopleTab.click();
    return this;
  }

  @Step("Открыть вкладку Income invitations")
  public PeoplePage openIncomeInvitationsTab() {
    incomeInvitationsTab.click();
    return this;
  }

  @Step("Открыть вкладку Outcome invitations")
  public PeoplePage openOutcomeInvitationsTab() {
    outcomeInvitationsTab.click();
    return this;
  }

  @Step("Найти пользователя: {username}")
  public PeoplePage searchPeople(String username) {
    searchInput.fill(username);
    page.waitForResponse(
        response -> response.url().contains("/graphql")
            && response.request().postData() != null
            && response.request().postData().contains("GetPeople")
            && response.ok(),
        searchButton::click);
    return this;
  }

  @Step("Отправить заявку пользователю: {username}")
  public PeoplePage clickAddForUser(String username) {
    Locator row = page.getByRole(AriaRole.ROW).filter(
        new Locator.FilterOptions().setHasText(username));
    Locator add = row.getByRole(AriaRole.BUTTON,
        new Locator.GetByRoleOptions().setName("Add").setExact(true));
    page.waitForResponse(
        response -> response.url().contains("/graphql")
            && response.request().postData() != null
            && response.request().postData().contains("FriendshipAction")
            && response.ok(),
        add::click);
    return this;
  }

  @Step("Проверить, что на странице People виден пользователь: {username}")
  public PeoplePage checkThatUserVisible(String username) {
    assertThat(heading).isVisible();
    assertThat(page.getByText(username, new Page.GetByTextOptions().setExact(true))).isVisible();
    return this;
  }

  @Step("Проверить число видимых пользователей: {count}")
  public PeoplePage checkThatUsersCount(int count) {
    assertThat(page.getByRole(AriaRole.ROW)).hasCount(count + 1);
    return this;
  }

  @Step("Проверить, что во вкладке только пользователи: {usernames}")
  public PeoplePage checkThatPeople(List<String> usernames) {
    assertThat(peopleRows).hasCount(usernames.size());
    usernames.forEach(username -> assertThat(row(username)).isVisible());
    return this;
  }

  @Step("Проверить, что пользователя нет во вкладке: {username}")
  public PeoplePage checkThatUserAbsent(String username) {
    assertThat(row(username)).hasCount(0);
    return this;
  }

  @Step("Проверить, что у пользователя {username} видна кнопка Remove")
  public PeoplePage checkThatRemoveButtonVisible(String username) {
    assertThat(button(username, "Remove")).isVisible();
    return this;
  }

  @Step("Проверить, что у пользователя {username} видны кнопки Accept и Decline")
  public PeoplePage checkThatAcceptAndDeclineVisible(String username) {
    assertThat(button(username, "Accept")).isVisible();
    assertThat(button(username, "Decline")).isVisible();
    return this;
  }

  @Step("Проверить, что у пользователя {username} видны Waiting... и кнопка Cancel")
  public PeoplePage checkThatWaitingAndCancelVisible(String username) {
    assertThat(row(username).getByText("Waiting...", new Locator.GetByTextOptions().setExact(true))).isVisible();
    assertThat(button(username, "Cancel")).isVisible();
    return this;
  }

  @Step("Проверить, что вкладка пуста")
  public PeoplePage checkThatNoUsers() {
    assertThat(emptyState).isVisible();
    assertThat(peopleRows).hasCount(0);
    return this;
  }

  private Locator row(String username) {
    return peopleRows.filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL,
        new Page.GetByRoleOptions().setName(username).setExact(true))));
  }

  private Locator button(String username, String name) {
    return row(username).getByRole(AriaRole.BUTTON,
        new Locator.GetByRoleOptions().setName(name).setExact(true));
  }
}
