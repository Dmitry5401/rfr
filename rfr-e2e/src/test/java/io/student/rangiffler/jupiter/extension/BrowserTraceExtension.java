package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.impl.junit.BrowserContextExtension;
import io.qameta.allure.playwright.AllurePlaywright;
import io.student.rangiffler.config.TestConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Starts Playwright tracing for each {@code @UiTest} and attaches {@code trace.zip} on failure.
 *
 * <p>Must stop tracing in {@link AfterEachCallback} (before Playwright's {@code TestWatcher}
 * closes the {@link BrowserContext}). Must not call
 * {@link BrowserContextExtension#getOrCreateBrowserContext} in afterEach: if the ThreadLocal was
 * already cleared, that allocates a fresh context and {@code tracing().stop()} then races with
 * close / throws {@code TargetClosedError} on the shared Playwright connection.
 *
 * <p>The context reference is kept in a non-{@link AutoCloseable} holder so JUnit's store
 * autoclose ({@code junit.jupiter.extensions.store.close.autocloseable.enabled=true}) does not
 * double-close the fixture-owned context.
 *
 * <p>Sources are off: with {@code setSources(true)} every Playwright API call also pushes a Java
 * stack into the driver ({@code Connection} tracingCount). Under
 * {@code parallel.mode.default=concurrent} that IPC load commonly kills the Windows driver pipe
 * ({@code DriverException} / {@code Failed to read message from driver}). Screenshots + snapshots
 * remain. Disable entirely with {@code -Drfr.browserTrace=false}.
 */
public final class BrowserTraceExtension implements BeforeEachCallback, AfterEachCallback {
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(BrowserTraceExtension.class);
  private static final String TRACE_PATH = "tracePath";
  private static final String CONTEXT = "context";
  private static final String STARTED = "started";

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    if (!TestConfig.browserTrace()) {
      return;
    }
    BrowserContext browser = BrowserContextExtension.getOrCreateBrowserContext(context);
    Path trace = Files.createTempDirectory("rfr-trace-").resolve("trace.zip");
    context.getStore(NAMESPACE).put(TRACE_PATH, trace);
    // Holder: BrowserContext is AutoCloseable; storing it raw would auto-close the fixture context.
    context.getStore(NAMESPACE).put(CONTEXT, new ContextRef(browser));
    try {
      browser.tracing().start(new Tracing.StartOptions()
          .setScreenshots(true)
          .setSnapshots(true)
          .setSources(false));
      context.getStore(NAMESPACE).put(STARTED, Boolean.TRUE);
    } catch (PlaywrightException ignored) {
      // Driver already dead (pipe closed) — leave STARTED unset so afterEach is a no-op.
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {
    if (!Boolean.TRUE.equals(context.getStore(NAMESPACE).get(STARTED, Boolean.class))) {
      return;
    }
    ContextRef ref = context.getStore(NAMESPACE).get(CONTEXT, ContextRef.class);
    if (ref == null || ref.context == null) {
      return;
    }
    Path trace = context.getStore(NAMESPACE).get(TRACE_PATH, Path.class);
    try {
      if (context.getExecutionException().isPresent()) {
        if (trace != null) {
          ref.context.tracing().stop(new Tracing.StopOptions().setPath(trace));
          AllurePlaywright.attachTrace("trace.zip", trace);
        } else {
          ref.context.tracing().stop();
        }
      } else {
        ref.context.tracing().stop();
      }
    } catch (PlaywrightException ignored) {
      // TargetClosedError / DriverException — Playwright TestWatcher closes next.
    }
  }

  /** Non-AutoCloseable so ExtensionContext.Store does not close the fixture BrowserContext. */
  private static final class ContextRef {
    final BrowserContext context;

    ContextRef(BrowserContext context) {
      this.context = context;
    }
  }
}
