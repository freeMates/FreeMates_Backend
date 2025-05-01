package jombi.freemates.config;

import java.util.Arrays;
import java.util.List;

public class SecurityUrls {

  /**
   * 인증을 생략할 URL 패턴 목록
   */
  public static final List<String> AUTH_WHITELIST = Arrays.asList(
      // API
      "/api/auth/register",             // 회원가입
      "/api/auth/login/app",
      "/api/auth/login/web",        // 로그인
      "/api/auth/duplicate/username", // 아이디 중복
      "/api/auth/refresh/web",
      "/api/auth/refresh/app",
      "/api/test/**",                  //FIXME: 테스트 API
      "/api/mail/send",                // 이메일 보내기
      "/api/mail/verify/**",           // 이메일 인증
      "/api/mail/check-verification/**",  // 이메일 인증 확인
      "/api/temp/**",                  //FIXME: 테스트 API

      // Swagger
      "/docs/**",                      // Swagger UI
      "/v3/api-docs/**",               // Swagger API 문서

      // WEB
      "/",                           // 기본 화면 > 로그인화면 리다이렉트
      "/login",                      // 로그인 화면
      "/register",                   // 회원가입 화면
      "/mail/verification-confirm",  // 이메일 인증 성공 화면
      "/mail/verification-fail",     // 이메일 인증 실패 화면

      // Static Resources
      "/static/**",                       // CSS 파일
      "/css/**",                       // CSS 파일
      "/fonts/**",                     // 폰트 파일
      "/images/**",                    // 이미지 파일
      "/js/**",                        // JS 파일
      "/firebase-messaging-sw.js",

      // SEO
      "/robots.txt",                   // 크롤링 허용 URL 파일
      "/sitemap.xml",                  // 페이지 URL 파일
      "/favicon.ico"                   // 아이콘 파일
  );

  /**
   * 관리자 권한이 필요한 URL 패턴 목록
   */
  public static final List<String> ADMIN_PATHS = Arrays.asList("/admin/**");
}
