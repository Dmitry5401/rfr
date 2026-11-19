package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.student.rangiffler.jupiter.extension.MigrationSuiteExtension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;

class MigrationSuiteExtensionTest {

  @Test
  void registeredThroughServiceLoader() throws IOException {
    String listing;
    try (InputStream in = getClass().getResourceAsStream(
        "/META-INF/services/org.junit.jupiter.api.extension.Extension")) {
      listing = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    assertTrue(listing.contains(MigrationSuiteExtension.class.getName()), listing);
    assertTrue(ServiceLoader.load(Extension.class).stream()
        .anyMatch(provider -> provider.type().equals(MigrationSuiteExtension.class)));
  }

  @Test
  void staysQuietWhenTheE2eTaskDidNotAsk() {
    String previous = System.getProperty("rfr.validateMigrations");
    System.setProperty("rfr.validateMigrations", "false");
    try {
      new MigrationSuiteExtension().beforeSuite(null);
    } finally {
      if (previous == null) {
        System.clearProperty("rfr.validateMigrations");
      } else {
        System.setProperty("rfr.validateMigrations", previous);
      }
    }
  }
}
