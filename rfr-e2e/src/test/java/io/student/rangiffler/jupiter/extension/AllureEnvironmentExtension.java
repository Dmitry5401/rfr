package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.stand.Migrations;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class AllureEnvironmentExtension implements SuiteExtension {
  @Override
  public void afterSuite(ExtensionContext context) {
    Map<String, String> environment = new LinkedHashMap<>();
    environment.put("frontUrl", TestConfig.frontUrl());
    environment.put("authUrl", TestConfig.authUrl());
    environment.put("apiUrl", TestConfig.apiUrl());
    environment.put("browser", TestConfig.browser());
    environment.put("headless", String.valueOf(TestConfig.headless()));
    environment.put("commit", TestConfig.commit());
    Arrays.stream(Databases.values()).forEach(database -> environment.put(database.schema(), version(new Migrations(database))));
    Path directory = Path.of(context.getConfigurationParameter("allure.results.directory").orElse("build/allure-results"));
    try {
      Files.createDirectories(directory);
      Files.write(
          directory.resolve("environment.properties"),
          environment.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toList());
    } catch (IOException failure) {
      throw new UncheckedIOException("Allure environment could not be written to " + directory.toAbsolutePath(), failure);
    }
  }

  private String version(Migrations migrations) {
    try {
      return migrations.version();
    } catch (RuntimeException failure) {
      return "unavailable";
    }
  }
}
