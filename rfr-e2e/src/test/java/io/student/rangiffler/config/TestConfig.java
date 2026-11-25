package io.student.rangiffler.config;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

public final class TestConfig {
  private TestConfig() {
  }

  public static String frontUrl() {
    return value("frontUrl", "http://localhost:3001");
  }

  public static String authUrl() {
    return value("authUrl", "http://localhost:9001");
  }

  public static String apiUrl() {
    return value("apiUrl", "http://localhost:8081");
  }

  public static String dbUrl(String schema) {
    return value("dbUrl", "jdbc:mysql://localhost:3306") + "/" + schema + "?serverTimezone=UTC";
  }

  public static String dbUser() {
    return value("dbUser", "root");
  }

  public static String dbPassword() {
    return value("dbPassword", "secret");
  }

  public static URI dbAddress() {
    return URI.create(value("dbUrl", "jdbc:mysql://localhost:3306").substring("jdbc:".length()));
  }

  public static boolean standCheck() {
    return Boolean.parseBoolean(value("standCheck", "true"));
  }

  public static Duration standTimeout() {
    return Duration.ofSeconds(Long.parseLong(value("standTimeout", "180")));
  }

  public static String browser() {
    return value("browser", "chromium");
  }

  public static boolean headless() {
    return Boolean.parseBoolean(value("headless", "true"));
  }

  public static int viewportWidth() {
    return Integer.parseInt(value("viewportWidth", "1920"));
  }

  public static int viewportHeight() {
    return Integer.parseInt(value("viewportHeight", "1080"));
  }

  public static String testIdAttribute() {
    return value("testIdAttribute", "data-test-id");
  }

  public static Duration actionTimeout() {
    return Duration.ofSeconds(Long.parseLong(value("actionTimeout", "5")));
  }

  public static Duration navigationTimeout() {
    return Duration.ofSeconds(Long.parseLong(value("navigationTimeout", "30")));
  }

  public static boolean migrationCheck() {
    return Boolean.parseBoolean(value("migrationCheck", "false"));
  }

  /** Short git commit hash from Gradle {@code -Drfr.commit}; tests must not run git themselves. */
  public static String commit() {
    return value("commit", "unknown");
  }

  public static Path migrations(String schema) {
    return Path.of(value("projectDir", ".."))
        .resolve("rfr-" + schema.substring("rangiffler-".length()))
        .resolve("src/main/resources/db/migration")
        .resolve(schema);
  }

  static String value(String key, String fallback) {
    String property = System.getProperty("rfr." + key);
    if (property != null && !property.isBlank()) {
      return property;
    }
    String variable = System.getenv("RFR_" + key.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ROOT));
    if (variable != null && !variable.isBlank()) {
      return variable;
    }
    return fallback;
  }
}
