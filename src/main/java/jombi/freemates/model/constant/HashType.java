package jombi.freemates.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HashType {
  GITHUB_ISSUES("관리되는 Gihub Issue 에 대한 전체 해시값"),
  SERVER_ERROR_CODES("스프링 서버 에러코드 해시값");

  private final String description;
}
