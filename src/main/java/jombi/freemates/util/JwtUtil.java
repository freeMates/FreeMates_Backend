package jombi.freemates.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtUtil {

  private final CustomUserDetailsService customUserDetailsService;

  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.access-exp-time}")
  private Long accessTokenExpTime; // AccessToken 만료 시간

  @Value("${jwt.refresh-exp-time}")
  private Long refreshTokenExpTime; // RefreshToken 만료 시간

  private static final String ACCESS_CATEGORY = "access";
  private static final String REFRESH_CATEGORY = "refresh";

  // 토큰에서 username 파싱
  public String getUsername(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("username", String.class);
  }

  /**
   * AccessToken 생성
   *
   * @param customUserDetails
   * @return
   */
  public String createAccessToken(CustomUserDetails customUserDetails) {
    log.info("엑세스 토큰 생성 중: 회원: {}", customUserDetails.getUsername());
    return createToken(ACCESS_CATEGORY, customUserDetails, accessTokenExpTime);
  }

  /**
   * RefreshToken 생성
   *
   * @param customUserDetails
   * @return
   */
  public String createRefreshToken(CustomUserDetails customUserDetails) {
    log.info("리프래시 토큰 생성 중: 회원: {}", customUserDetails.getUsername());
    return createToken(REFRESH_CATEGORY, customUserDetails, refreshTokenExpTime);
  }

  /**
   * JWT 토큰 생성 메서드
   *
   * @param customUserDetails 회원 상세 정보
   * @param expiredAt         만료 시간
   * @return 생성된 JWT 토큰
   */
  private String createToken(String category, CustomUserDetails customUserDetails, Long expiredAt) {

    return Jwts.builder()
        .issuer("AlomSpring")
        .subject(customUserDetails.getUsername())
        .claim("category", category)
        .claim("username", customUserDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiredAt))
        .signWith(getSignKey())
        .compact();
  }

  /**
   * JWT 토큰 유효성 검사
   *
   * @param token 검증할 JWT 토큰
   * @return 유효 여부
   */
  public boolean validateToken(String token) throws ExpiredJwtException {
    try {
      Jwts.parser()
          .verifyWith(getSignKey())
          .build()
          .parseSignedClaims(token);
      log.info("JWT 토큰이 유효합니다.");
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
      throw e; // 만료된 토큰 예외를 호출한 쪽으로 전달
    } catch (UnsupportedJwtException e) {
      log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.warn("형식이 잘못된 JWT 토큰입니다: {}", e.getMessage());
    } catch (SignatureException e) {
      log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
    }
    return false;
  }

  /**
   * JWT 서명에 사용할 키 생성
   *
   * @return SecretKey 객체
   */
  private SecretKey getSignKey() {
    try {
      // Base64 문자열로부터 SecretKey를 생성
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (IllegalArgumentException e) {
      log.error("비밀 키 생성 실패: {}", e.getMessage());
      throw e; // 예외 재발생
    }
  }

  /**
   * JWT 토큰에서 클레임 (Claims) 추출
   *
   * @param token JWT 토큰
   * @return 추출된 클레임
   */
  public Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * 리프레시 토큰 만료 시간 반환
   *
   * @return 리프레시 토큰 만료 시간 (밀리초 단위)
   */
  public long getRefreshExpirationTime() {
    return refreshTokenExpTime;
  }

}