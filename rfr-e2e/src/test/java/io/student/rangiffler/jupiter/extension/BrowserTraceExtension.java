package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.impl.junit.BrowserContextExtension;
import io.qameta.allure.playwright.AllurePlaywright;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class BrowserTraceExtension implements BeforeEachCallback, AfterEachCallback {
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(BrowserTraceExtension.class);
  private static final String TRACE_PATH = "tracePath";

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    BrowserContext browser = BrowserContextExtension.getOrCreateBrowserContext(context);
    Path trace = Files.createTempDirectory("rfr-trace-").resolve("trace.zip");
    context.getStore(NAMESPACE).put(TRACE_PATH, trace);
    browser.tracing().start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true)
        .setSources(true));
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    BrowserContext browser = BrowserContextExtension.getOrCreateBrowserContext(context);
    Path trace = context.getStore(NAMESPACE).get(TRACE_PATH, Path.class);
    if (context.getExecutionException().isPresent()) {
      browser.tracing().stop(new Tracing.StopOptions().setPath(trace));
      AllurePlaywright.attachTrace("trace.zip", trace);
    } else {
      browser.tracing().stop();
    }
  }
}
