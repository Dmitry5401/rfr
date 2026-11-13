package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record AuthAccount(String username) {
  private static final JdbcClient AUTH = Jdbc.client(Databases.AUTH);

  public void create(String encodedPassword) {
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

  public void delete() {
    Jdbc.inTransaction(Databases.AUTH, () -> {
      AUTH.sql(Jdbc.sql("auth/delete-authorities.sql")).param("username", username).update();
      AUTH.sql(Jdbc.sql("auth/delete-user.sql")).param("username", username).update();
    });
  }

  public boolean exists() {
    return SqlSupport.exists(AUTH, "auth/exist-user.sql", "username", username);
  }
}
