package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Friendships(String username) {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  public void create(String friendUsername) {
    new Friendship(username, friendUsername).create();
  }

  public void delete() {
    API.sql(Jdbc.sql("api/delete-friends.sql")).param("username", username).update();
  }

  public boolean exists() {
    return SqlSupport.exists(API, "api/exist-friends.sql", "username", username);
  }
}
