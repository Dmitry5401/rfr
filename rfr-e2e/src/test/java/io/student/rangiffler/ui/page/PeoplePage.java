package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.PeopleTable;

public final class PeoplePage {
  public static final String URL = TestConfig.frontUrl() + "/people";
  private final Locator friends;
  private final Locator allPeople;
  private final Locator outcomeInvitations;
  private final Locator incomeInvitations;
  private final Locator search;
  private final PeopleTable table;

  public PeoplePage(Page page) {
    this.friends = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("Friends").setExact(true));
    this.allPeople = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("All People").setExact(true));
    this.outcomeInvitations = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("Outcome invitations").setExact(true));
    this.incomeInvitations = page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName("Income invitations").setExact(true));
    this.search = page.getByRole(AriaRole.TEXTBOX,
        new Page.GetByRoleOptions().setName("search people").setExact(true));
    this.table = new PeopleTable(page);
  }

  @Step("Открыть вкладку Friends")
  public PeoplePage clickFriends() {
    friends.click();
    return this;
  }

  @Step("Открыть вкладку All People")
  public PeoplePage clickAllPeople() {
    allPeople.click();
    return this;
  }

  @Step("Открыть вкладку Outcome invitations")
  public PeoplePage clickOutcomeInvitations() {
    outcomeInvitations.click();
    return this;
  }

  @Step("Открыть вкладку Income invitations")
  public PeoplePage clickIncomeInvitations() {
    incomeInvitations.click();
    return this;
  }

  @Step("Найти пользователя {username}")
  public PeoplePage searchPeople(String username) {
    search.fill(username);
    search.press("Enter");
    return this;
  }

  public PeopleTable table() {
    return table;
  }
}
