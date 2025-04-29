package jombi.freemates.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "refresh_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RefreshToken {
  // 사용자 식별
  @Id
  private String username;

  // 토큰
  @Column( nullable = false)
  private String refreshToken;

  public void update(String newToken) {
    this.refreshToken = newToken;
  }





}
