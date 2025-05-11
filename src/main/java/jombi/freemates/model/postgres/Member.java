package jombi.freemates.model.postgres;

import jakarta.persistence.*;
import java.util.UUID;
import jombi.freemates.model.constant.Gender;
import jombi.freemates.model.constant.Role;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID memberId;

  @Column(nullable = false, unique = true)
  private String username; // 로그인 ID

  @Column(nullable = false)
  private String password;

  private Integer birthYear;

  @Enumerated(EnumType.STRING)
  private Gender gender; // MALE, FEMALE

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String nickname;

  private Role role;

  private boolean isDeleted = false; // 탈퇴 되었는지 확인

  public void markDeleted() {
    this.isDeleted = true;
  }

}
