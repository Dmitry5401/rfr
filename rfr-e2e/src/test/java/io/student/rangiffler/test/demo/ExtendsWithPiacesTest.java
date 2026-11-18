package io.student.rangiffler.test.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("@ExtendWith")
class ExtendsWithPiacesTest {

  @Nested
  @ExtendWith(DemoExtension.class)
  @DisplayName("на классе")
  class OnClass {

    @Test
    @DisplayName("аннотация на классе")
    void extensionOnClass(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на методе")
  class OnMethod {

    @Test
    @ExtendWith(DemoExtension.class)
    @DisplayName("аннотация на тестовом методе")
    void extensionOnMethod(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на статическом поле")
  class OnStaticField {

    @ExtendWith(DemoExtension.class)
    private static final Object MARKER = new Object();

    @Test
    @DisplayName("аннотация на static-поле")
    void extensionOnStaticField(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertTrue(MARKER != null);
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на поле экземпляра")
  class OnInstanceField {

    @ExtendWith(DemoExtension.class)
    private final Object marker = new Object();

    @Test
    @DisplayName("аннотация на поле экземпляра")
    void extensionOnInstanceField(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
      assertTrue(marker != null);
    }
  }

  @Nested
  @DisplayName("на параметре метода")
  class OnMethodParameter {

    @Test
    @DisplayName("аннотация на параметре теста")
    void extensionOnMethodParameter(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на параметре конструктора")
  class OnConstructorParameter {

    private final DemoToken token;

    OnConstructorParameter(@ExtendWith(DemoExtension.class) DemoToken token) {
      this.token = token;
    }

    @Test
    @DisplayName("аннотация на параметре конструктора")
    void extensionOnConstructorParameter() {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на суперклассе")
  class OnSuperclass extends ExtensionDeclaredOnSuperclass {

    @Test
    @DisplayName("аннотация унаследована от суперкласса")
    void extensionOnSuperclass(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на интерфейсе")
  class OnInterface implements ExtensionDeclaredOnInterface {

    @Test
    @DisplayName("аннотация пришла с интерфейса")
    void extensionOnInterface(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на параметре @BeforeEach")
  class OnBeforeEachParameter {

    @BeforeEach
    void setUp(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("аннотация на параметре @BeforeEach")
    void extensionOnBeforeEachParameter(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на параметре @AfterEach")
  class OnAfterEachParameter {

    @Test
    @DisplayName("аннотация на параметре @AfterEach")
    void extensionOnAfterEachParameter(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }

    @AfterEach
    void tearDown(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре @BeforeAll")
  class OnBeforeAllParameter {

    @BeforeAll
    static void beforeAll(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("аннотация на параметре @BeforeAll")
    void extensionOnBeforeAllParameter(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeAll");
      assertCalled("beforeEach");
    }
  }

  @Nested
  @DisplayName("на параметре @AfterAll")
  class OnAfterAllParameter {

    @Test
    @DisplayName("аннотация на параметре @AfterAll")
    void extensionOnAfterAllParameter(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertCalled("beforeEach");
    }

    @AfterAll
    static void afterAll(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @WithDemoExtension
  @DisplayName("на составной аннотации")
  class OnComposedAnnotation {

    @Test
    @DisplayName("аннотация на составной аннотации")
    void extensionOnComposedAnnotation() {
      assertNotCalled("аннотация на составной аннотации", "beforeEach");
    }
  }

  @Nested
  @DisplayName("на методе @BeforeEach")
  class OnBeforeEachMethod {

    @BeforeEach
    @ExtendWith(DemoExtension.class)
    void setUp() {
    }

    @Test
    @DisplayName("аннотация на методе @BeforeEach")
    void extensionOnBeforeEachMethod() {
      assertNotCalled("аннотация на методе @BeforeEach", "beforeEach");
    }
  }

  @Nested
  @DisplayName("на методе @AfterEach")
  class OnAfterEachMethod {

    @Test
    @DisplayName("аннотация на методе @AfterEach")
    void extensionOnAfterEachMethod() {
      assertNotCalled("аннотация на методе @AfterEach", "beforeEach");
    }

    @AfterEach
    @ExtendWith(DemoExtension.class)
    void tearDown() {
    }
  }

  @Nested
  @DisplayName("на методе @BeforeAll")
  class OnBeforeAllMethod {

    @BeforeAll
    @ExtendWith(DemoExtension.class)
    static void beforeAll() {
    }

    @Test
    @DisplayName("аннотация на методе @BeforeAll")
    void extensionOnBeforeAllMethod() {
      assertNotCalled("аннотация на методе @BeforeAll", "beforeAll");
      assertNotCalled("аннотация на методе @BeforeAll", "beforeEach");
    }
  }

  @Nested
  @DisplayName("на методе @AfterAll")
  class OnAfterAllMethod {

    @Test
    @DisplayName("аннотация на методе @AfterAll")
    void extensionOnAfterAllMethod() {
      assertNotCalled("аннотация на методе @AfterAll", "beforeEach");
    }

    @AfterAll
    @ExtendWith(DemoExtension.class)
    static void afterAll() {
    }
  }

  @AfterEach
  void extensionRanAroundTheTest(TestInfo info) {
    String name = info.getDisplayName();
    if (DemoExtension.callbacks().stream().noneMatch(line -> line.startsWith(name + ":beforeEach"))) {
      return;
    }
    assertCalledFor(name, "beforeTestExecution");
    assertCalledFor(name, "afterTestExecution");
    if (info.getTestMethod().orElseThrow().getParameterCount() > 0) {
      assertCalledFor(name, "resolveParameter");
    }
  }

  private static void assertCalled(String callback) {
    assertTrue(DemoExtension.callbacks().stream().anyMatch(line -> line.endsWith(":" + callback)), callback);
  }

  private static void assertCalledFor(String displayName, String callback) {
    assertTrue(DemoExtension.callbacks().contains(displayName + ":" + callback), displayName + ":" + callback);
  }

  private static void assertNotCalled(String displayName, String callback) {
    assertTrue(DemoExtension.callbacks().stream().noneMatch(line -> line.equals(displayName + ":" + callback)),
        displayName + ":" + callback);
  }
}

@ExtendWith(DemoExtension.class)
abstract class ExtensionDeclaredOnSuperclass {
}

@ExtendWith(DemoExtension.class)
interface ExtensionDeclaredOnInterface {
}

@ExtendWith(DemoExtension.class)
@interface WithDemoExtension {
}
