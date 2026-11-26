package io.student.rangiffler.jupiter.annotation;

import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.jupiter.extension.BrowserOptions;
import io.student.rangiffler.jupiter.extension.BrowserTimeoutExtension;
import io.student.rangiffler.jupiter.extension.BrowserTraceExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * UI tests: Playwright fixtures + timeouts + optional tracing.
 *
 * <p>{@link ExecutionMode#SAME_THREAD} matches Playwright Java guidance — methods of one class
 * share ThreadLocal Browser/Playwright safely; classes still run concurrently via
 * {@code junit-platform.properties}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("ui")
@Execution(ExecutionMode.SAME_THREAD)
@UsePlaywright(BrowserOptions.class)
@ExtendWith({BrowserTimeoutExtension.class, BrowserTraceExtension.class})
public @interface UiTest {
}
