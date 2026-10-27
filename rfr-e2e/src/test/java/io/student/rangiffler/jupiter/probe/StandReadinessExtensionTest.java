package io.student.rangiffler.jupiter.probe;

import com.sun.net.httpserver.HttpServer;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import io.student.rangiffler.stand.HttpService;
import io.student.rangiffler.stand.Stand;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("StandReadinessExtension")
class StandReadinessExtensionTest {
  @Test
  @DisplayName("Сервис начал отвечать через 2 с — тесты выполняются")
  void testsRunWhenServiceRespondsLater() {
    long start = System.nanoTime();
    HttpServer server = server(() -> System.nanoTime() - start > Duration.ofSeconds(2).toNanos(), new AtomicInteger());
    try {
      long succeeded = run(server, true, Duration.ofSeconds(10), Guarded.class).testEvents().succeeded().count();
      assertEquals(1L, succeeded, "Tests did not run after the service started to respond");
    } finally {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("Сервис не ответил за таймаут — тесты не выполняются")
  void testsDontRunWhenServiceStaysSilent() {
    HttpServer server = server(() -> false, new AtomicInteger());
    try {
      long started = run(server, true, Duration.ofMillis(700), Guarded.class).testEvents().started().count();
      assertEquals(0L, started, "Tests started although the service never responded");
    } finally {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("Сервис не ответил за таймаут — в ошибке есть имя сервиса")
  void failureNamesSilentService() {
    HttpServer server = server(() -> false, new AtomicInteger());
    try {
      boolean named = run(server, true, Duration.ofMillis(700), Guarded.class).containerEvents().failed().stream()
          .map(event -> event.getRequiredPayload(TestExecutionResult.class))
          .flatMap(result -> result.getThrowable().stream())
          .anyMatch(failure -> String.valueOf(failure.getMessage()).contains("rfr-stub-7319"));
      assertTrue(named, "Failure of a silent stand does not name the silent service");
    } finally {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("Проверка выключена — к сервису нет ни одного запроса")
  void disabledCheckSendsNoRequests() {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = server(() -> true, requests);
    try {
      run(server, false, Duration.ofSeconds(3), Guarded.class);
      assertEquals(0, requests.get(), "Disabled stand check still sent requests to the service");
    } finally {
      server.stop(0);
    }
  }

  @Test
  @DisplayName("Класс без тега UI-тестов не ждёт стенд — к сервису нет ни одного запроса")
  void untaggedClassSendsNoRequests() {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = server(() -> true, requests);
    try {
      run(server, true, Duration.ofSeconds(3), Untagged.class);
      assertEquals(0, requests.get(), "Class without the UI tag still waited for the stand");
    } finally {
      server.stop(0);
    }
  }

  private HttpServer server(Ready ready, AtomicInteger requests) {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
      server.createContext("/", exchange -> {
        requests.incrementAndGet();
        exchange.sendResponseHeaders(ready.now() ? 200 : 503, -1);
        exchange.close();
      });
      server.start();
      return server;
    } catch (IOException failure) {
      throw new UncheckedIOException("Stub service could not start on an ephemeral port", failure);
    }
  }

  private EngineExecutionResults run(HttpServer server, boolean check, Duration timeout, Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .configurationParameter(StubbedStand.PORT, String.valueOf(server.getAddress().getPort()))
        .configurationParameter(StubbedStand.CHECK, String.valueOf(check))
        .configurationParameter(StubbedStand.TIMEOUT, String.valueOf(timeout.toMillis()))
        .selectors(selectClass(target))
        .execute();
  }

  interface Ready {
    boolean now();
  }

  static final class StubbedStand implements BeforeAllCallback {
    static final String PORT = "rfr.probe.stand.port";
    static final String CHECK = "rfr.probe.stand.check";
    static final String TIMEOUT = "rfr.probe.stand.timeout";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
      new StandReadinessExtension(
          context.getConfigurationParameter(CHECK, Boolean::parseBoolean).orElseThrow(),
          "probe-stand",
          new Stand(
              List.of(new HttpService(
                  "rfr-stub-7319",
                  "start the stub",
                  URI.create("http://127.0.0.1:" + context.getConfigurationParameter(PORT).orElseThrow()))),
              Duration.ofMillis(context.getConfigurationParameter(TIMEOUT, Long::parseLong).orElseThrow()),
              Duration.ofMillis(100)))
          .beforeAll(context);
    }
  }

  @ProbeTarget
  @Tag("probe-stand")
  @ExtendWith(StubbedStand.class)
  static final class Guarded {
    @Test
    void runs() {
    }
  }

  @ProbeTarget
  @ExtendWith(StubbedStand.class)
  static final class Untagged {
    @Test
    void runs() {
    }
  }
}
