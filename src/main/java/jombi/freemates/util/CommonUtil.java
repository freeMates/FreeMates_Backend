package jombi.freemates.util;

import com.github.javafaker.Faker;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.UUID;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {

  private static final Faker faker = new Faker();

  public static String getRandomName() {
    return faker.funnyName().name() + "-" + UUID.randomUUID().toString().substring(0, 5);
  }

  /**
   * 문자열 SHA-256 해시 계산
   */
  public static String calculateSha256ByStr(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("SHA-256 해시 계산 실패", e);
    }
  }

  /**
   * 파일 SHA-256 해시값 계산
   */
  public static String calculateFileHash(Path filePath) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] fileBytes = Files.readAllBytes(filePath);
      byte[] hashBytes = digest.digest(fileBytes);
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("파일 해시 계산 실패", e);
    }
  }

  /**
   * null 문자 처리 -> str1이 null 인 경우 str2 반환
   * "null" 문자열 처리 -> str1이 "null" 인 경우 str2 반환
   * str1이 빈 문자열 or 공백인 경우 -> str2 반환
   *
   * @param str1 검증할 문자열
   * @param str2 str1 이 null 인경우 반환할 문자열
   * @return null 이 아닌 문자열
   */
  public static String nvl(String str1, String str2) {
    if (str1 == null) { // str1 이 null 인 경우
      return str2;
    } else if (str1.equals("null")) { // str1 이 문자열 "null" 인 경우
      return str2;
    } else if (str1.isBlank()) { // str1 이 "" or " " 인 경우
      return str2;
    }
    return str1;
  }

  /**
   * Integer val 값이 null 인 경우 0으로 변환 후 반환
   *
   * @param val 검증할 Integer 래퍼클래스 정수 val
   * @return null 이 아닌 정수 값
   */
  public static int null2ZeroInt(Integer val) {
    if (val == null) { // val 이 null 인경우 0 반환
      return 0;
    }
    return val;
  }

  /**
   * Validation 검증 : Object 값 -> null , Object가 문자열인 경우 -> 비어있으면 예외 발생
   *
   * @param value   검증할 값 (String인 경우 빈 문자열 체크 포함)
   * @param message 에러 메시지
   */
  public static void checkNotNullOrEmpty(Object value, String message) {
    if (value == null) {
      log.error(message);
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    if (value instanceof String && ((String) value).isEmpty()) {
      log.error(message);
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
  }
}
