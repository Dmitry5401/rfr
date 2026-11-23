package io.student.rangiffler.suite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit Platform Suite that selects UI e2e tests as one runnable unit.
 * Stand readiness / schema / Allure environment stay on SuiteExtension via ServiceLoader.
 */
@Suite
@SelectPackages("io.student.rangiffler.test")
@IncludeTags("ui")
public final class E2eSuite {
  private E2eSuite() {
  }
}
