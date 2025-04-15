package jombi.freemates.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jombi.freemates.model.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)//알 수없는 필드 무시
public class RegisterRequest {

  private String username;

  private String password;

  private Integer birthYear;

  private Gender gender;

  private String email;

  private String nickname;

}