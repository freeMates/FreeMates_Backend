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

  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다"),

  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),

  INVALID_AGE(HttpStatus.BAD_REQUEST, "잘못된 나이입니다."),

  // Login
  REGISTRATION_EXPIRED(HttpStatus.NOT_FOUND, "키값이 만료되었습니다. "),

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다"),

  PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

  ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access 토큰이 만료되었습니다."),

  REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh 토큰이 만료되었거나 존재하지 않습니다."),

  INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 Refresh 토큰입니다"),

  // Delete
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

  ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다."),

  // Email
  INVALID_EMAIL(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일입니다"),

  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),

  // Bookmark
  BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 즐겨찾기입니다."),
  DUPLICATE_PLACE_IN_BOOKMARK(HttpStatus.CONFLICT, "이미 즐겨찾기에 추가된 장소입니다."),

  // Place
  PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),

  COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 코스입니다."),

  // File
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 파일입니다."),


  ;

  private final HttpStatus status;
  private final String message;
}
