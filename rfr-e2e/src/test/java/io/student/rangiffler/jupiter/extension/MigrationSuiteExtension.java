package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.SchemaMigrations;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class MigrationSuiteExtension implements SuiteExtension {

  @Override
  public void beforeSuite(ExtensionContext context) {
    if (!TestConfig.validateMigrations()) {
      return;
    }
    SchemaMigrations.validate();
  }
}
