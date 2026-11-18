package io.student.rangiffler.test.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

  @AfterEach
  void extensionRanAroundTheTest() {
    assertCalled("beforeTestExecution");
    assertCalled("afterTestExecution");
    assertCalled("resolveParameter");
  }

  private static void assertCalled(String callback) {
    assertTrue(DemoExtension.callbacks().stream().anyMatch(line -> line.endsWith(":" + callback)), callback);
  }
}

@ExtendWith(DemoExtension.class)
abstract class ExtensionDeclaredOnSuperclass {
}

@ExtendWith(DemoExtension.class)
interface ExtensionDeclaredOnInterface {
}
