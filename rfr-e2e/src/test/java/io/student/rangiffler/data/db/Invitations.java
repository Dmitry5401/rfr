package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Invitations(String username) {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  public void create(String requesterUsername) {
    new Invitation(requesterUsername, username).create();
  }

  public void delete() {
    API.sql(Jdbc.sql("api/delete-invitations.sql")).param("username", username).update();
  }

  public boolean exists() {
    return SqlSupport.exists(API, "api/exist-invitations.sql", "username", username);
  }
}
