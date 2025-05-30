package jombi.freemates.model.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class RegisterResponse {

  private UUID memberId;

  private String email;

  private String nickname;

  private String username;
}