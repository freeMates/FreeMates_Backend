package jombi.freemates.config;

import java.util.Arrays;
import java.util.List;

public class SecurityUrls {

  /**
   * 인증을 생략할 URL 패턴 목록
   */
  public static final List<String> AUTH_WHITELIST = Arrays.asList(
      // API
      "/api/auth/login", // 로그인
      "/api/auth/register", // 회원가입

      // Swagger
      "/docs/**", // Swagger UI
      "/v3/api-docs/**" // Swagger API 문서
  );

  /**
   * 허용된 CORS Origin 목록
   */
  public static final List<String> ALLOWED_ORIGINS = Arrays.asList(

  );
}
