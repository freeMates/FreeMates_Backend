package jombi.freemates.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import jombi.freemates.model.constant.JwtTokenType;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.repository.MemberRepository;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.issuer}")
  private String issuer;

  private Key getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // token 생성 저장 : ACCESS, REFRESH 따로
  public String generateToken(Authentication authentication, JwtTokenType jwtTokenType) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    String username = userDetails.getMember().getUsername();
    Date now = new Date();

    Date expiryDate = new Date(now.getTime() + jwtTokenType.getDurationMilliseconds());

    log.info("{} 토큰 생성: 사용자 이름 {}", jwtTokenType, username);
    log.debug("토큰 유효 기간: {}부터 {}까지", now, expiryDate);

    return Jwts.builder()
        .setSubject(username)
        .setIssuer(issuer)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
  }

  // JWT Token -> username
  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();

    String username = claims.getSubject();
    log.debug("토큰에서 사용자 이름 추출: {}", username);
    return username;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      log.debug("JWT 토큰 검증 성공");
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      // 토큰 검증 실패 시
      log.error("JWT 토큰 검증 실패: {}", e.getMessage());
    }
    return false;
  }
}