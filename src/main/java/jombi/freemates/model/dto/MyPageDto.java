package jombi.freemates.model.dto;

import jombi.freemates.model.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageDto {
  private String nickname;
  private String email;
  private Integer age;
  private Gender gender;
  private String username;


}
