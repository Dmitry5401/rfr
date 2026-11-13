package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Friendship(Profile requester, Profile addressee, FriendshipStatus status) implements DbEntity {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  @Override
  public void create() {
    SqlSupport.requireOneRow(
        API.sql(Jdbc.sql("api/create-friendship.sql"))
            .param("requester", requester.username())
            .param("addressee", addressee.username())
            .param("status", status.name())
            .update(),
        "friendship " + requester.username() + " -> " + addressee.username() + " [" + status + "]");
  }

  @Override
  public void delete() {
    API.sql(Jdbc.sql("api/delete-friendship.sql"))
        .param("requester", requester.username())
        .param("addressee", addressee.username())
        .param("status", status.name())
        .update();
  }

  @Override
  public boolean exists() {
    return SqlSupport.exists(
        API,
        "api/exist-friendship.sql",
        requester.username(),
        addressee.username(),
        status);
  }
}
