package jombi.freemates.util.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // Global

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다"),

  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

  // Register

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다"),

  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다"),

  PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}
