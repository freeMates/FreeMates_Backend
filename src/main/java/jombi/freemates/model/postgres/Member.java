package jombi.freemates.model.postgres;

import jakarta.persistence.*;
import java.util.UUID;
import jombi.freemates.model.constant.Role;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BasePostgresEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID memberId;

  @Column(nullable = false, unique = true)
  private String username; // JWT 토큰 subject

  @Column(nullable = false)
  private String password;

  private Integer birthYear;

  private Integer gender;

  private Long phone;

  private String nickname;

  private Role role;
}
