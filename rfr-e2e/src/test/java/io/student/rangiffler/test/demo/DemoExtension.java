package io.student.rangiffler.test.demo;

import java.util.ArrayList;
import java.util.List;
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

  private static final List<String> CALLBACKS = new ArrayList<>();

  public static List<String> callbacks() {
    synchronized (CALLBACKS) {
      return List.copyOf(CALLBACKS);
    }
  }

  public static void clear() {
    synchronized (CALLBACKS) {
      CALLBACKS.clear();
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    trace(context, "beforeAll");
  }

  @Override
  public void afterAll(ExtensionContext context) {
    trace(context, "afterAll");
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    trace(context, "beforeEach");
  }

  @Override
  public void afterEach(ExtensionContext context) {
    trace(context, "afterEach");
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    trace(context, "beforeTestExecution");
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    trace(context, "afterTestExecution");
  }

  @Override
  public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
    trace(context, "preConstructTestInstance");
  }

  @Override
  public void preDestroyTestInstance(ExtensionContext context) {
    trace(context, "preDestroyTestInstance");
  }

  @Override
  public void beforeClassTemplateInvocation(ExtensionContext context) {
    trace(context, "beforeClassTemplateInvocation");
  }

  @Override
  public void afterClassTemplateInvocation(ExtensionContext context) {
    trace(context, "afterClassTemplateInvocation");
  }

  @Override
  public void beforeThreadInterrupt(PreInterruptContext preInterruptContext, ExtensionContext context) {
    trace(context, "beforeThreadInterrupt");
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == DemoToken.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    trace(extensionContext, "resolveParameter");
    return new DemoToken(extensionContext.getDisplayName());
  }

  private static void trace(ExtensionContext context, String callback) {
    String line = context.getDisplayName() + ":" + callback;
    synchronized (CALLBACKS) {
      CALLBACKS.add(line);
    }
    context.publishReportEntry("callback", line);
  }
}
