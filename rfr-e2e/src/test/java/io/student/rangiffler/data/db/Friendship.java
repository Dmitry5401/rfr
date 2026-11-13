package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Friendship(String requester, String addressee) {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  public void create() {
    SqlSupport.requireOneRow(
        API.sql(Jdbc.sql("api/create-friendship.sql"))
            .param("requester", requester)
            .param("addressee", addressee)
            .update(),
        "friendship " + requester + " -> " + addressee);
  }

  public void delete() {
    API.sql(Jdbc.sql("api/delete-friendship.sql"))
        .param("requester", requester)
        .param("addressee", addressee)
        .update();
  }

  public boolean exists() {
    return SqlSupport.exists(
        API, "api/exist-friendship.sql", "requester", requester, "addressee", addressee);
  }
}
