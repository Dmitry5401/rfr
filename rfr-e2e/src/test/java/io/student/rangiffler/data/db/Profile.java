package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Profile(String username) implements DbEntity {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  @Override
  public void create() {
    SqlSupport.requireOneRow(
        API.sql(Jdbc.sql("api/create-profile.sql"))
            .param("username", username)
            .update(),
        "api profile " + username);
  }

  @Override
  public void delete() {
    API.sql(Jdbc.sql("api/delete-profile.sql")).param("username", username).update();
  }

  @Override
  public boolean exists() {
    return SqlSupport.exists(API, "api/exist-profile.sql", "username", username);
  }

  public String id() {
    return API.sql(Jdbc.sql("api/profile-id.sql"))
        .param("username", username)
        .query(String.class)
        .single();
  }
}
