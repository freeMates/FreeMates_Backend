package jombi.freemates.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PinColor {
  RED("빨강"),
  YELLOW("노랑"),
  GREEN("초록"),
  BLUE("파랑"),
  PURPLE("보라"),
  PINK("핑크");

  private final String description;
}
