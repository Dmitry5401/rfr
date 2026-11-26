package io.student.rangiffler.data;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.MySQLDialect;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserEntityTest {

  @Test
  void registerSetsAccountFlagsAndBothRoles() {
    UserEntity user = UserEntity.register("alice", "{bcrypt}hash");

    assertEquals("alice", user.getUsername());
    assertEquals("{bcrypt}hash", user.getPassword());
    assertTrue(user.getEnabled());
    assertTrue(user.getAccountNonExpired());
    assertTrue(user.getAccountNonLocked());
    assertTrue(user.getCredentialsNonExpired());
    assertEquals(
        Set.of(Authority.read, Authority.write),
        user.getAuthorities().stream().map(AuthorityEntity::getAuthority).collect(Collectors.toSet())
    );
    assertTrue(user.getAuthorities().stream().allMatch(authority -> authority.getUser() == user));
  }

  @Test
  void registerRejectsMissingCredentials() {
    assertThrows(NullPointerException.class, () -> UserEntity.register(null, "{bcrypt}hash"));
    assertThrows(NullPointerException.class, () -> UserEntity.register("alice", null));
  }

  @Test
  void authoritiesAreLoadedLazily() throws NoSuchFieldException {
    OneToMany mapping = UserEntity.class.getDeclaredField("authorities").getAnnotation(OneToMany.class);

    assertEquals(FetchType.LAZY, mapping.fetch());
  }

  @Test
  void hibernateAcceptsTheAuthorityMapping() {
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .applySetting("hibernate.dialect", MySQLDialect.class.getName())
        .applySetting("jakarta.persistence.schema-generation.database.action", "none")
        .build();
    try {
      new MetadataSources(registry)
          .addAnnotatedClass(UserEntity.class)
          .addAnnotatedClass(AuthorityEntity.class)
          .buildMetadata();
    } finally {
      StandardServiceRegistryBuilder.destroy(registry);
    }
  }

  @Test
  void returnedAuthoritiesCannotChangeThePersistentList() {
    UserEntity user = UserEntity.register("alice", "{bcrypt}hash");

    assertThrows(UnsupportedOperationException.class, () -> user.getAuthorities().clear());
    assertEquals(2, user.getAuthorities().size());
  }

  @Test
  void removeAuthorityDropsTheRoleAndTheBackReference() {
    UserEntity user = UserEntity.register("alice", "{bcrypt}hash");
    AuthorityEntity read = user.getAuthorities().stream()
        .filter(authority -> authority.getAuthority() == Authority.read)
        .findFirst()
        .orElseThrow();

    user.removeAuthority(read);

    assertEquals(Set.of(Authority.write), user.getAuthorities().stream()
        .map(AuthorityEntity::getAuthority)
        .collect(Collectors.toSet()));
    assertNull(read.getUser());
  }
}
