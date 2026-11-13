package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record AuthAccount(String username, String encodedPassword) implements DbEntity {
  private static final JdbcClient AUTH = Jdbc.client(Databases.AUTH);

  @Override
  public void create() {
    Jdbc.inTransaction(Databases.AUTH, () -> {
      SqlSupport.requireOneRow(
          AUTH.sql(Jdbc.sql("auth/create-user.sql"))
              .param("username", username)
              .param("password", encodedPassword)
              .update(),
          "auth user " + username);
      AUTH.sql(Jdbc.sql("auth/create-authorities.sql"))
          .param("username", username)
          .update();
    });
  }

  @Override
  public void delete() {
    Jdbc.inTransaction(Databases.AUTH, () -> {
      AUTH.sql(Jdbc.sql("auth/delete-authorities.sql")).param("username", username).update();
      AUTH.sql(Jdbc.sql("auth/delete-user.sql")).param("username", username).update();
    });
  }

  @Override
  public boolean exists() {
    return SqlSupport.exists(AUTH, "auth/exist-user.sql", "username", username);
  }
}
