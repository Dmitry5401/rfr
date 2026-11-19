package io.student.rangiffler.suite;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("io.student.rangiffler")
@ExcludePackages("io.student.rangiffler.suite")
public class E2eSuite {
  private E2eSuite() {
  }

  @BeforeSuite
  static void checkMigrations() {
    if (TestConfig.migrationCheck()) {
      StandSchemaExtension.check();
    }
  }
}
