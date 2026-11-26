package io.student.rangiffler.jupiter.probe;

import com.microsoft.playwright.Page;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.PeopleTab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("BrowserTraceExtension red check")
class BrowserTraceRedCheckProbe {
  @Test
  @DisplayName("Broken People tab locator waits and attaches trace")
  void brokenPeopleTabWaits(Page page) {
    page.setContent("""
        <div role="tablist" aria-label="People tabs">
          <button role="tab">Friends</button>
          <button role="tab">All People</button>
          <button role="tab">Outcome invitations</button>
          <button role="tab">Income invitations</button>
        </div>
        """);
    new PeoplePage(page).openTab(PeopleTab.INCOME_INVITATIONS);
  }
}
