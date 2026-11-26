package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.PeopleTable;

public final class PeoplePage {
  public static final String URL = TestConfig.frontUrl() + "/people";
  private final Page page;
  private final Locator searchInput;
  private final Locator searchButton;
  private final PeopleTable table;

  public PeoplePage(Page page) {
    this.page = page;
    this.searchInput = page.getByLabel("search people");
    this.searchButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("search").setExact(true));
    this.table = new PeopleTable(page);
  }

  @Step("Открыть вкладку {tab.title}")
  public PeoplePage openTab(PeopleTab tab) {
    page.getByRole(AriaRole.TAB,
        new Page.GetByRoleOptions().setName(tab.title()).setExact(true)).click();
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

  public PeopleTable table() {
    return table;
  }
}
