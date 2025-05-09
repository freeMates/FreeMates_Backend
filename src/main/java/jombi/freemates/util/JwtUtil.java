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
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
    String memberId = userDetails.getMember().getMemberId().toString();
    Date now = new Date();

    Date expiryDate = new Date(now.getTime() + jwtTokenType.getDurationMilliseconds());

    return Jwts.builder()
        .setSubject(memberId)
        .setIssuer(issuer)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
  }

  // token -> memberId(UUID) 반환
  public UUID getMemberIdFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();

    String subject = claims.getSubject();
    try {
      return UUID.fromString(subject);
    } catch (IllegalArgumentException e) {
      log.error("Invalid UUID in token subject: {}", subject, e);
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
  }

  public UUID getMemberId() {
    // Authentication 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // 인증 정보가 없거나 인증되지 않았으면 예외 발생
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // MemberId 추출
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    return userDetails.getMember().getMemberId();
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
