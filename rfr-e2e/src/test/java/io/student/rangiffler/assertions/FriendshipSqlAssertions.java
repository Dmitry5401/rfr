package io.student.rangiffler.assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbc;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class FriendshipSqlAssertions {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  private FriendshipSqlAssertions() {
  }

  public static void assertFriendsCount(String username, int expected) {
    assertEquals(expected, count("api/count-friends.sql", username),
        () -> "ACCEPTED friendships for " + username);
  }

  public static void assertIncomeInvitationsCount(String username, int expected) {
    assertEquals(expected, count("api/count-income-invitations.sql", username),
        () -> "PENDING income invitations for " + username);
  }

  public static void assertNoRelations(String username) {
    assertEquals(0, count("api/count-relations.sql", username),
        () -> "friendship rows for " + username + " after fixture close");
  }

  private static int count(String sqlPath, String username) {
    Integer value = API.sql(Jdbc.sql(sqlPath))
        .param("username", username)
        .query(Integer.class)
        .single();
    return value == null ? 0 : value;
  }
}
