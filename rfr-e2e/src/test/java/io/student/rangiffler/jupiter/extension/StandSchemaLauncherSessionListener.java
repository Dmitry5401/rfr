package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Same schema check as {@link StandSchemaExtension}, run at launcher-session open.
 * Registered through {@code META-INF/services/org.junit.platform.launcher.LauncherSessionListener}.
 */
public final class StandSchemaLauncherSessionListener implements LauncherSessionListener {
  public static final String OPENED_MARKER = "StandSchemaLauncherSessionListener.launcherSessionOpened";

  private static final AtomicInteger OPENED = new AtomicInteger();

  public static int openedCount() {
    return OPENED.get();
  }

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    int count = OPENED.incrementAndGet();
    System.out.println(OPENED_MARKER + " count=" + count + " pid=" + ProcessHandle.current().pid());
    if (TestConfig.migrationCheck()) {
      StandSchemaExtension.check();
    }
  }
}
