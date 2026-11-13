package io.student.rangiffler.data.db;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;

public record Friendships(Profile profile, List<Friendship> items) implements DbEntity {
  private static final JdbcClient API = Jdbc.client(Databases.API);

  public Friendships(Profile profile) {
    this(profile, List.of());
  }

  @Override
  public void create() {
    items.forEach(Friendship::create);
  }

  @Override
  public void delete() {
    API.sql(Jdbc.sql("api/delete-friendships-by-profile.sql"))
        .param("profileId", profile.id())
        .param("status", FriendshipStatus.ACCEPTED.name())
        .update();
  }

  @Override
  public boolean exists() {
    return SqlSupport.exists(
        API,
        "api/exist-friendships-by-profile.sql",
        profile.id(),
        FriendshipStatus.ACCEPTED);
  }
}
