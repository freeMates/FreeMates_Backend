package jombi.freemates.model.dto;

import java.util.UUID;
import jombi.freemates.model.postgres.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class RegisterResponse {

  private Member member;
}