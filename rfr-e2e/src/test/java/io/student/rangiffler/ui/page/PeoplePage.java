package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeoplePage {
  public static final String URL = TestConfig.frontUrl() + "/people";
  private final Page page;
  private final Locator heading;
  private final Locator friendsTab;
  private final Locator allPeopleTab;
  private final Locator incomeInvitationsTab;
  private final Locator searchInput;
  private final Locator searchButton;

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
    this.searchInput = page.getByLabel("search people");
    this.searchButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("search").setExact(true));
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
}
