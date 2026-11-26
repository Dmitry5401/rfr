package io.student.rangiffler.data.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserEntityTest {

  @Test
  void createFixesUsernameAndCountry() {
    CountryEntity russia = country("ru");

    UserEntity user = UserEntity.create("alice", russia);

    assertEquals("alice", user.getUsername());
    assertSame(russia, user.getCountry());
    assertNull(user.getFirstname());
    assertNull(user.getLastName());
    assertNull(user.getAvatar());
  }

  @Test
  void createRejectsMissingUsernameOrCountry() {
    CountryEntity russia = country("ru");

    assertThrows(NullPointerException.class, () -> UserEntity.create(null, russia));
    assertThrows(NullPointerException.class, () -> UserEntity.create("alice", null));
  }

  @Test
  void updateProfileChangesOnlyProvidedFields() {
    CountryEntity russia = country("ru");
    CountryEntity france = country("fr");
    UserEntity user = UserEntity.create("alice", russia);
    user.updateProfile("Anna", "Ivanova", new byte[]{1, 2}, france);

    user.updateProfile(null, "Petrova", null, null);

    assertEquals("alice", user.getUsername());
    assertEquals("Anna", user.getFirstname());
    assertEquals("Petrova", user.getLastName());
    assertArrayEquals(new byte[]{1, 2}, user.getAvatar());
    assertSame(france, user.getCountry());
  }

  private static CountryEntity country(String code) {
    CountryEntity country = new CountryEntity();
    country.setCode(code);
    return country;
  }
}
