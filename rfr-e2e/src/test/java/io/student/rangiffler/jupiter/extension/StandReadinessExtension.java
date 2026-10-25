package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.HttpService;
import io.student.rangiffler.config.Stand;
import io.student.rangiffler.config.TcpService;
import io.student.rangiffler.config.TestConfig;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class StandReadinessExtension implements SuiteExtension {
  private final boolean enabled;
  private final String tag;
  private final Stand stand;

  public StandReadinessExtension() {
    this(
        TestConfig.standCheck(),
        "ui",
        new Stand(
            List.of(
                new TcpService("MySQL", "docker compose up -d --wait", TestConfig.dbAddress()),
                new HttpService(
                    "rfr-auth",
                    "./gradlew runAll",
                    URI.create(TestConfig.authUrl() + "/.well-known/openid-configuration")),
                new HttpService(
                    "rfr-api",
                    "./gradlew runAll",
                    HttpRequest.newBuilder(URI.create(TestConfig.apiUrl() + "/graphql"))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"{__typename}\"}"))
                        .build(),
                    status -> true),
                new HttpService("rfr-gql-client", "cd rfr-gql-client && npm run dev", URI.create(TestConfig.frontUrl()))),
            TestConfig.standTimeout(),
            Duration.ofSeconds(2)));
  }

  public StandReadinessExtension(boolean enabled, String tag, Stand stand) {
    this.enabled = enabled;
    this.tag = tag;
    this.stand = stand;
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (context.getTags().contains(tag)) {
      SuiteExtension.super.beforeAll(context);
    }
  }

  @Override
  public void beforeSuite(ExtensionContext context) {
    if (enabled) {
      stand.await();
    }
  }
}
