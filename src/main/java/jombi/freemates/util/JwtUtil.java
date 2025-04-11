package jombi.freemates.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import jombi.freemates.model.constant.JwtTokenType;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

  @Value("${jwt.secret-key}")
  private String secretKey;

  // 2025.02.09 : jwtToken 만료시간 ENUM Handling 으로 변경
  @Value("${jwt.access-exp-time}")
  private long accessTokenValidityInMilliseconds;

  // refresh 토큰 만료 시간 (밀리초)
  @Value("${jwt.refresh-exp-time}")
  private long refreshTokenValidityInMilliseconds;

  @Value("${jwt.issuer}")
  private String issuer;

  private Key getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // token 생성 저장 : ACCESS, REFRESH 따로
  public String generateToken(Authentication authentication, JwtTokenType jwtTokenType) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    String username = userDetails.getUsername();
    Date now = new Date();

    // 2025.02.09 : jwtToken 만료시간 ENUM Handling 으로 변경
//    long duration = (jwtTokenType == JwtTokenType.ACCESS)
//        ? accessTokenValidityInMilliseconds
//        : refreshTokenValidityInMilliseconds;

    Date expiryDate = new Date(now.getTime() + jwtTokenType.getDurationMilliseconds());

    return Jwts.builder()
        .setSubject(username)
        .setIssuer(issuer)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
  }

  // token -> username 반환
  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    try {
      return String.valueOf(claims.getSubject());
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      // 토큰 검증 실패 시
      log.error(e.getMessage(), e);
    }
    return false;
  }
}
