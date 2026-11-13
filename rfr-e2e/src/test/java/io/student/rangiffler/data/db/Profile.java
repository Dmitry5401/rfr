package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Profile(String username) {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  public void create() {
    SqlSupport.requireOneRow(
        API.sql(Jdbc.sql("api/create-profile.sql"))
            .param("username", username)
            .update(),
        "api profile " + username);
  }

  public void delete() {
    API.sql(Jdbc.sql("api/delete-profile.sql")).param("username", username).update();
  }

  public boolean exists() {
    return SqlSupport.exists(API, "api/exist-profile.sql", "username", username);
  }
}
