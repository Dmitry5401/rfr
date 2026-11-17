package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("UserExtension")
class UserExtensionTest {

  @Test
  @DisplayName("Тест с @User проходит, и TestReporter фиксирует резолв")
  void testWithUserSucceeds() {
    EngineExecutionResults results = run(Observed.class);

    assertEquals(1, results.testEvents().succeeded().count(), "Test with @User did not succeed");
    assertEquals("1", entriesOf(results).get("friends"));
    assertEquals("1", entriesOf(results).get("incomeInvitations"));
    assertEquals("1", entriesOf(results).get("outcomeInvitations"));
  }

  @Test
  @DisplayName("Во время теста пользователь есть в auth и api")
  void accountAndProfileExistDuringTest() {
    Map<String, String> entries = runAndReturnEntries(Observed.class);

    assertEquals("true", entries.get("account"), "Account did not exist during the test");
    assertEquals("true", entries.get("profile"), "Profile did not exist during the test");
  }

  @Test
  @DisplayName("Во время теста друг ACCEPTED, входящая заявка PENDING")
  void relationsHaveExpectedStatusDuringTest() {
    Map<String, String> entries = runAndReturnEntries(Observed.class);

    assertEquals("true", entries.get("friendship"), "Accepted friendship did not exist during the test");
    assertEquals("true", entries.get("invitation"), "Pending invitation did not exist during the test");
    assertEquals("true", entries.get("outcomeInvitation"), "Outgoing invitation did not exist during the test");
  }

  @Test
  @DisplayName("После теста Store закрывает пользователя, друга, пригласившего и приглашённого")
  void storeClosesFixtureAfterTest() {
    Map<String, String> entries = runAndReturnEntries(Observed.class);

    assertFalse(account(entries.get("username")).exists(), "Account was not removed after the test");
    assertFalse(new ApiProfile(entries.get("username")).exists(), "Profile was not removed after the test");
    assertFalse(new ApiProfile(entries.get("friend")).exists(), "Friend profile was not removed after the test");
    assertFalse(new ApiProfile(entries.get("inviter")).exists(), "Inviter profile was not removed after the test");
    assertFalse(new ApiProfile(entries.get("invitee")).exists(), "Invitee profile was not removed after the test");
  }

  @Test
  @DisplayName("После упавшего теста Store всё равно закрывает фикстуру")
  void storeClosesFixtureAfterFailedTest() {
    EngineExecutionResults results = run(Failing.class);
    Map<String, String> entries = entriesOf(results);

    assertEquals(1, results.testEvents().failed().count(), "Failing probe did not fail");
    assertFalse(entries.get("username").isBlank(), "TestReporter did not record the resolved user");
    assertFalse(new ApiProfile(entries.get("username")).exists(), "Profile was not removed after the failed test");
    assertFalse(account(entries.get("username")).exists(), "Account was not removed after the failed test");
  }

  @Test
  @DisplayName("Два параметра @User получают разных пользователей")
  void twoParametersGetDifferentUsers() {
    EngineExecutionResults results = run(TwoUsers.class);
    Map<String, String> entries = entriesOf(results);

    assertNotEquals(entries.get("first"), entries.get("second"), "Two @User parameters got the same user");
    assertEquals("true", entries.get("firstAccount"));
    assertEquals("true", entries.get("secondAccount"));
    assertFalse(new ApiProfile(entries.get("first")).exists(), "First user was not removed");
    assertFalse(new ApiProfile(entries.get("second")).exists(), "Second user was not removed");
  }

  @Test
  @DisplayName("Отрицательное число друзей валит тест до тела и до репорта")
  void negativeFriendsFailBeforeTheTestBody() {
    EngineExecutionResults results = run(NegativeFriends.class);

    assertInstanceOf(IllegalArgumentException.class, failure(results, IllegalArgumentException.class),
        "Negative friends count did not fail the test with IllegalArgumentException");
    assertTrue(entriesOf(results).isEmpty(), "Test body ran after an invalid @User");
  }

  @Test
  @DisplayName("TestUser без @User не резолвится")
  void userWithoutAnnotationIsNotResolved() {
    EngineExecutionResults results = run(Unannotated.class);

    assertInstanceOf(ParameterResolutionException.class, failure(results, ParameterResolutionException.class),
        "TestUser without @User was resolved");
    assertTrue(entriesOf(results).isEmpty(), "Test body ran without @User");
  }

  @Test
  @DisplayName("@User в параметре @BeforeEach не резолвится")
  void userInBeforeEachIsNotResolved() {
    EngineExecutionResults results = run(BeforeEachUser.class);

    assertInstanceOf(ParameterResolutionException.class, failure(results, ParameterResolutionException.class),
        "@User in @BeforeEach parameter was resolved");
    assertTrue(entriesOf(results).isEmpty(), "@BeforeEach body ran with a resolved @User");
  }

  private EngineExecutionResults run(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(target))
        .execute();
  }

  private Map<String, String> runAndReturnEntries(Class<?> target) {
    return entriesOf(run(target));
  }

  private Map<String, String> entriesOf(EngineExecutionResults results) {
    Map<String, String> entries = new HashMap<>();
    results.allEvents().reportingEntryPublished().stream()
        .map(event -> event.getRequiredPayload(ReportEntry.class))
        .forEach(entry -> entries.putAll(entry.getKeyValuePairs()));
    return entries;
  }

  private Throwable failure(EngineExecutionResults results, Class<? extends Throwable> type) {
    return results.testEvents().failed().stream()
        .map(event -> event.getRequiredPayload(TestExecutionResult.class))
        .flatMap(result -> result.getThrowable().stream())
        .map(error -> findInChain(error, type))
        .filter(type::isInstance)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Probe did not fail with " + type.getSimpleName()));
  }

  private Throwable findInChain(Throwable error, Class<? extends Throwable> type) {
    Throwable current = error;
    while (current != null) {
      if (type.isInstance(current)) {
        return current;
      }
      current = current.getCause();
    }
    return error;
  }

  private AuthAccount account(String username) {
    return new AuthAccount(username, "ignored");
  }

  @ProbeTarget
  static final class Observed {
    @Test
    void observe(TestReporter reporter, @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
      TestUser friend = user.friends().getFirst();
      TestUser inviter = user.incomeInvitations().getFirst();
      TestUser invitee = user.outcomeInvitations().getFirst();
      reporter.publishEntry(Map.of(
          "username", user.username(),
          "friend", friend.username(),
          "inviter", inviter.username(),
          "invitee", invitee.username(),
          "friends", String.valueOf(user.friends().size()),
          "incomeInvitations", String.valueOf(user.incomeInvitations().size()),
          "outcomeInvitations", String.valueOf(user.outcomeInvitations().size())));
      reporter.publishEntry(Map.of(
          "account", String.valueOf(new AuthAccount(user.username(), user.password()).exists()),
          "profile", String.valueOf(new ApiProfile(user.username()).exists()),
          "friendship", String.valueOf(new Friendship(
              new ApiProfile(user.username()),
              new ApiProfile(friend.username()),
              FriendshipStatus.ACCEPTED).exists()),
          "invitation", String.valueOf(new Friendship(
              new ApiProfile(inviter.username()),
              new ApiProfile(user.username()),
              FriendshipStatus.PENDING).exists()),
          "outcomeInvitation", String.valueOf(new Friendship(
              new ApiProfile(user.username()),
              new ApiProfile(invitee.username()),
              FriendshipStatus.PENDING).exists())));
    }
  }

  @ProbeTarget
  static final class Failing {
    @Test
    void fail(TestReporter reporter, @User TestUser user) {
      reporter.publishEntry("username", user.username());
      throw new IllegalStateException("Probe target fails on purpose");
    }
  }

  @ProbeTarget
  static final class TwoUsers {
    @Test
    void observe(TestReporter reporter, @User TestUser first, @User TestUser second) {
      reporter.publishEntry(Map.of(
          "first", first.username(),
          "second", second.username(),
          "firstAccount", String.valueOf(new AuthAccount(first.username(), first.password()).exists()),
          "secondAccount", String.valueOf(new AuthAccount(second.username(), second.password()).exists())));
    }
  }

  @ProbeTarget
  static final class NegativeFriends {
    @Test
    void observe(TestReporter reporter, @User(friends = -1) TestUser user) {
      reporter.publishEntry("username", user.username());
    }
  }

  @ProbeTarget
  static final class Unannotated {
    @Test
    void observe(TestReporter reporter, TestUser user) {
      reporter.publishEntry("username", user.username());
    }
  }

  @ProbeTarget
  static final class BeforeEachUser {
    @BeforeEach
    void prepare(TestReporter reporter, @User TestUser user) {
      reporter.publishEntry("username", user.username());
    }

    @Test
    void observe() {
    }
  }
}
