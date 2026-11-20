package io.student.rangiffler.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "\"user\"")
public class UserEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column
  private String firstname;

  @Column(name = "last_name")
  private String lastName;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] avatar;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "country_id", referencedColumnName = "id")
  private CountryEntity country;

  protected UserEntity() {
  }

  public static UserEntity create(String username, CountryEntity country) {
    UserEntity user = new UserEntity();
    user.username = Objects.requireNonNull(username, "username");
    user.country = Objects.requireNonNull(country, "country");
    return user;
  }

  public void updateProfile(String firstname, String lastName, byte[] avatar, CountryEntity country) {
    if (firstname != null) {
      this.firstname = firstname;
    }
    if (lastName != null) {
      this.lastName = lastName;
    }
    if (avatar != null) {
      this.avatar = avatar;
    }
    if (country != null) {
      this.country = country;
    }
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    UserEntity that = (UserEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
