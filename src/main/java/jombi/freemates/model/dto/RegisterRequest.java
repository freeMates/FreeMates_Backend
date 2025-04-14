package jombi.freemates.model.dto;

import jombi.freemates.model.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class RegisterRequest {

  private String username;

  private String password;

  private Integer birthYear;

  private Gender gender;

  private String email;

  private String nickname;

}