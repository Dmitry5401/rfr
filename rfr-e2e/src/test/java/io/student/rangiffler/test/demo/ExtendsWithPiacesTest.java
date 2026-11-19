package io.student.rangiffler.test.demo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("@ExtendWith")
class ExtendsWithPiacesTest {

  private static final Map<String, Integer> INSTANCES = new LinkedHashMap<>();

  static {
    INSTANCES.put("на классе", 1);
    INSTANCES.put("на методе", 2);
    INSTANCES.put("на статическом поле", 1);
    INSTANCES.put("на поле экземпляра", 1);
    INSTANCES.put("на параметре метода", 2);
    INSTANCES.put("на параметре конструктора", 1);
    INSTANCES.put("на суперклассе", 1);
    INSTANCES.put("на интерфейсе", 1);
    INSTANCES.put("на параметре @BeforeEach", 1);
    INSTANCES.put("на параметре @AfterEach", 1);
    INSTANCES.put("на параметре @BeforeAll", 1);
    INSTANCES.put("на параметре @AfterAll", 1);
    INSTANCES.put("на составной аннотации", 0);
    INSTANCES.put("на методе @BeforeEach", 0);
    INSTANCES.put("на методе @AfterEach", 0);
    INSTANCES.put("на методе @BeforeAll", 0);
    INSTANCES.put("на методе @AfterAll", 0);
  }

  @Nested
  @ExtendWith(DemoExtension.class)
  @DisplayName("на классе")
  class OnClass extends DemoCase {

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на методе")
  class OnMethod extends DemoCase {

    @Test
    @ExtendWith(DemoExtension.class)
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @ExtendWith(DemoExtension.class)
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на статическом поле")
  class OnStaticField extends DemoCase {

    @ExtendWith(DemoExtension.class)
    private static final Object MARKER = new Object();

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertTrue(MARKER != null);
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на поле экземпляра")
  class OnInstanceField extends DemoCase {

    @ExtendWith(DemoExtension.class)
    private final Object marker = new Object();

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
      assertTrue(marker != null);
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре метода")
  class OnMethodParameter extends DemoCase {

    @Test
    @DisplayName("первый")
    void first(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре конструктора")
  class OnConstructorParameter extends DemoCase {

    private final DemoToken token;

    OnConstructorParameter(@ExtendWith(DemoExtension.class) DemoToken token) {
      this.token = token;
    }

    @Test
    @DisplayName("первый")
    void first() {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second() {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на суперклассе")
  class OnSuperclass extends ExtensionDeclaredOnSuperclass {

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на интерфейсе")
  class OnInterface extends DemoCase implements ExtensionDeclaredOnInterface {

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре @BeforeEach")
  class OnBeforeEachParameter extends DemoCase {

    @BeforeEach
    void setUp(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре @AfterEach")
  class OnAfterEachParameter extends DemoCase {

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @AfterEach
    void tearDown(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре @BeforeAll")
  class OnBeforeAllParameter extends DemoCase {

    @BeforeAll
    static void beforeAll(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @DisplayName("на параметре @AfterAll")
  class OnAfterAllParameter extends DemoCase {

    @Test
    @DisplayName("первый")
    void first(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @Test
    @DisplayName("второй")
    void second(DemoToken token) {
      assertFalse(token.source().isBlank());
    }

    @AfterAll
    static void afterAll(@ExtendWith(DemoExtension.class) DemoToken token) {
      assertFalse(token.source().isBlank());
    }
  }

  @Nested
  @WithDemoExtension
  @DisplayName("на составной аннотации")
  class OnComposedAnnotation extends DemoCase {

    @Test
    @DisplayName("первый")
    void first() {
      assertNotRegistered("на составной аннотации");
    }

    @Test
    @DisplayName("второй")
    void second() {
      assertNotRegistered("на составной аннотации");
    }
  }

  @Nested
  @DisplayName("на методе @BeforeEach")
  class OnBeforeEachMethod extends DemoCase {

    @BeforeEach
    @ExtendWith(DemoExtension.class)
    void setUp() {
    }

    @Test
    @DisplayName("первый")
    void first() {
      assertNotRegistered("на методе @BeforeEach");
    }

    @Test
    @DisplayName("второй")
    void second() {
      assertNotRegistered("на методе @BeforeEach");
    }
  }

  @Nested
  @DisplayName("на методе @AfterEach")
  class OnAfterEachMethod extends DemoCase {

    @Test
    @DisplayName("первый")
    void first() {
      assertNotRegistered("на методе @AfterEach");
    }

    @Test
    @DisplayName("второй")
    void second() {
      assertNotRegistered("на методе @AfterEach");
    }

    @AfterEach
    @ExtendWith(DemoExtension.class)
    void tearDown() {
    }
  }

  @Nested
  @DisplayName("на методе @BeforeAll")
  class OnBeforeAllMethod extends DemoCase {

    @BeforeAll
    @ExtendWith(DemoExtension.class)
    static void beforeAll() {
    }

    @Test
    @DisplayName("первый")
    void first() {
      assertNotRegistered("на методе @BeforeAll");
    }

    @Test
    @DisplayName("второй")
    void second() {
      assertNotRegistered("на методе @BeforeAll");
    }
  }

  @Nested
  @DisplayName("на методе @AfterAll")
  class OnAfterAllMethod extends DemoCase {

    @Test
    @DisplayName("первый")
    void first() {
      assertNotRegistered("на методе @AfterAll");
    }

    @Test
    @DisplayName("второй")
    void second() {
      assertNotRegistered("на методе @AfterAll");
    }

    @AfterAll
    @ExtendWith(DemoExtension.class)
    static void afterAll() {
    }
  }

  @AfterEach
  void extensionRanAroundTheTest(TestInfo info) {
    String place = className(info) + " / " + info.getDisplayName();
    if (DemoExtension.trace().stream().noneMatch(line -> line.place().equals(place) && line.callback().equals("beforeEach"))) {
      return;
    }
    assertTrue(seen(place, "beforeTestExecution"), place);
    assertTrue(seen(place, "afterTestExecution"), place);
    if (info.getTestMethod().orElseThrow().getParameterCount() > 0) {
      assertTrue(seen(place, "resolveParameter"), place);
    }
  }

  static void report(String place) {
    Set<Integer> ids = ids(place);
    assertEquals(INSTANCES.get(place), ids.size(), place + " " + DemoExtension.trace());
    if (INSTANCES.get(place) != 1) {
      System.out.println(place + ": " + verdict(ids));
    }
  }

  static void announce(String place) {
    if (INSTANCES.containsKey(place)) {
      System.out.println(place + ": " + verdict(ids(place)));
    }
  }

  private static String className(TestInfo info) {
    return info.getTestClass()
        .map(type -> type.getAnnotation(DisplayName.class))
        .map(DisplayName::value)
        .orElse("");
  }

  private static boolean seen(String place, String callback) {
    return DemoExtension.trace().stream().anyMatch(line -> line.place().equals(place) && line.callback().equals(callback));
  }

  private static void assertNotRegistered(String place) {
    assertEquals(Set.of(), ids(place), place);
  }

  private static Set<Integer> ids(String place) {
    return DemoExtension.trace().stream()
        .filter(line -> line.place().equals(place) || line.place().startsWith(place + " / "))
        .map(DemoExtension.Trace::id)
        .collect(Collectors.toSet());
  }

  private static String verdict(Set<Integer> ids) {
    if (ids.isEmpty()) {
      return "не создаётся";
    }
    if (ids.size() == 1) {
      return "один экземпляр на оба теста " + ids;
    }
    return "новый экземпляр на каждый тест " + ids;
  }
}

@TestMethodOrder(MethodOrderer.MethodName.class)
abstract class DemoCase {

  @AfterAll
  static void report(TestInfo info) {
    ExtendsWithPiacesTest.report(info.getDisplayName());
  }
}

@ExtendWith(DemoExtension.class)
abstract class ExtensionDeclaredOnSuperclass extends DemoCase {
}

@ExtendWith(DemoExtension.class)
interface ExtensionDeclaredOnInterface {
}

@ExtendWith(DemoExtension.class)
@interface WithDemoExtension {
}
