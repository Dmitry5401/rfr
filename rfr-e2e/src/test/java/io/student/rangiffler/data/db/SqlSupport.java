package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

final class SqlSupport {
  private SqlSupport() {
  }

  static void requireOneRow(int updated, String what) {
    if (updated != 1) {
      throw new IllegalStateException("Expected one inserted row for " + what + ", got " + updated);
    }
  }

  static boolean exists(JdbcClient client, String sqlPath, String paramName, String paramValue) {
    Integer count = client.sql(Jdbc.sql(sqlPath))
        .param(paramName, paramValue)
        .query(Integer.class)
        .single();
    return count != null && count > 0;
  }

  static boolean exists(
      JdbcClient client,
      String sqlPath,
      String profileId,
      FriendshipStatus status) {
    Integer count = client.sql(Jdbc.sql(sqlPath))
        .param("profileId", profileId)
        .param("status", status.name())
        .query(Integer.class)
        .single();
    return count != null && count > 0;
  }

  static boolean exists(
      JdbcClient client,
      String sqlPath,
      String requester,
      String addressee,
      FriendshipStatus status) {
    Integer count = client.sql(Jdbc.sql(sqlPath))
        .param("requester", requester)
        .param("addressee", addressee)
        .param("status", status.name())
        .query(Integer.class)
        .single();
    return count != null && count > 0;
  }
}
