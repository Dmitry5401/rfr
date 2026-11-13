package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public record Invitation(String requester, String addressee) {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  public void create() {
    SqlSupport.requireOneRow(
        API.sql(Jdbc.sql("api/create-invitation.sql"))
            .param("requester", requester)
            .param("addressee", addressee)
            .update(),
        "invitation " + requester + " -> " + addressee);
  }

  public void delete() {
    API.sql(Jdbc.sql("api/delete-invitation.sql"))
        .param("requester", requester)
        .param("addressee", addressee)
        .update();
  }

  public boolean exists() {
    return SqlSupport.exists(
        API, "api/exist-invitation.sql", "requester", requester, "addressee", addressee);
  }
}
