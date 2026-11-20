package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.stand.Migrations;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Writes Allure {@code environment.properties} once per JVM suite in {@link #afterSuite}.
 *
 * <p>Registered through {@code META-INF/services} ahead of readiness/schema extensions so its
 * {@code SuiteRun} is stored before a failing {@code beforeSuite} can skip later {@code beforeAll}
 * callbacks — {@code afterSuite} still runs via store autoclose.
 */
public final class AllureEnvironmentExtension implements SuiteExtension {
  public static final String RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
  public static final String DEFAULT_RESULTS_DIRECTORY = "build/allure-results";
  public static final String ENVIRONMENT_FILE = "environment.properties";

  @Override
  public void afterSuite(ExtensionContext context) {
    write(resultsDirectory(), environment());
  }

  public Map<String, String> environment() {
    Map<String, String> values = new LinkedHashMap<>();
    values.put("frontUrl", TestConfig.frontUrl());
    values.put("authUrl", TestConfig.authUrl());
    values.put("apiUrl", TestConfig.apiUrl());
    values.put("browser", TestConfig.browser());
    values.put("headless", String.valueOf(TestConfig.headless()));
    values.put("commit", TestConfig.commit());
    for (Databases database : Databases.values()) {
      values.put(database.schema(), schemaVersion(database));
    }
    return values;
  }

  public static Path resultsDirectory() {
    return Path.of(System.getProperty(RESULTS_DIRECTORY_PROPERTY, DEFAULT_RESULTS_DIRECTORY));
  }

  public static void write(Path resultsDirectory, Map<String, String> values) {
    try {
      Files.createDirectories(resultsDirectory);
      String body = values.entrySet().stream()
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining("\n", "", "\n"));
      Files.writeString(resultsDirectory.resolve(ENVIRONMENT_FILE), body, StandardCharsets.UTF_8);
    } catch (IOException failure) {
      throw new UncheckedIOException("Could not write Allure " + ENVIRONMENT_FILE, failure);
    }
  }

  private static String schemaVersion(Databases database) {
    try {
      return new Migrations(database).version();
    } catch (RuntimeException ignored) {
      return "unavailable";
    }
  }
}
