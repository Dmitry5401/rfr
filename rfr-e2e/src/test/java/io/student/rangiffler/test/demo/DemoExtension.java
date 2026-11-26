package io.student.rangiffler.test.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.PreInterruptCallback;
import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

public final class DemoExtension implements
    BeforeAllCallback,
    AfterAllCallback,
    BeforeEachCallback,
    AfterEachCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    TestInstancePreConstructCallback,
    TestInstancePreDestroyCallback,
    BeforeClassTemplateInvocationCallback,
    AfterClassTemplateInvocationCallback,
    PreInterruptCallback,
    ParameterResolver {

  public record Trace(int id, String place, String callback) {
  }

  private static final AtomicInteger NEXT_ID = new AtomicInteger();
  private static final List<Trace> TRACE = new ArrayList<>();
  private final int id = NEXT_ID.incrementAndGet();

  public DemoExtension() {
    System.out.println("DemoExtension#" + id + " created");
  }

  public static List<Trace> trace() {
    synchronized (TRACE) {
      return List.copyOf(TRACE);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    log("beforeAll", context);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    log("afterAll", context);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    log("beforeEach", context);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    log("afterEach", context);
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    log("beforeTestExecution", context);
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    log("afterTestExecution", context);
  }

  @Override
  public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
    log("preConstructTestInstance", context);
  }

  @Override
  public void preDestroyTestInstance(ExtensionContext context) {
    log("preDestroyTestInstance", context);
  }

  @Override
  public void beforeClassTemplateInvocation(ExtensionContext context) {
    log("beforeClassTemplateInvocation", context);
  }

  @Override
  public void afterClassTemplateInvocation(ExtensionContext context) {
    log("afterClassTemplateInvocation", context);
  }

  @Override
  public void beforeThreadInterrupt(PreInterruptContext preInterruptContext, ExtensionContext context) {
    log("beforeThreadInterrupt", context);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == DemoToken.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    String parameter = parameterContext.getDeclaringExecutable().getName() + "#" + parameterContext.getIndex();
    log("resolveParameter", extensionContext, parameter);
    return new DemoToken("#" + id);
  }

  private void log(String callback, ExtensionContext context) {
    log(callback, context, "");
  }

  private void log(String callback, ExtensionContext context, String detail) {
    log(callback, place(context), detail);
  }

  private void log(String callback, String place, String detail) {
    synchronized (TRACE) {
      TRACE.add(new Trace(id, place, callback));
    }
    String line = "DemoExtension#" + id + " " + callback;
    if (!detail.isEmpty()) {
      line += " " + detail;
    }
    System.out.println(line);
  }

  private static String place(ExtensionContext context) {
    String current = context.getDisplayName();
    return context.getParent()
        .map(parent -> parent.getDisplayName() + " / " + current)
        .orElse(current);
  }
}
