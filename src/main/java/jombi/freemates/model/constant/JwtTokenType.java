package jombi.freemates.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JwtTokenType {
  ACCESS(3600000L), // 1시간
  REFRESH(604800000L); // 7일
  private final Long durationMilliseconds;
  public long getDurationMilliseconds() {
    return durationMilliseconds;
  }
}
