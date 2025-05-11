package jombi.freemates.util.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import jombi.freemates.config.SecurityUrls;
import jombi.freemates.service.CustomUserDetailsService;
import jombi.freemates.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public JwtAuthenticationFilter(JwtUtil jwtUtil,
      CustomUserDetailsService customUserDetailsService) {
    this.jwtUtil = jwtUtil;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // AUTH_WHITELIST URL -> JWT 인증 안함
    String requestUri = request.getRequestURI();
    boolean isWhitelisted = SecurityUrls.AUTH_WHITELIST.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    if (isWhitelisted) {
      log.debug("허용된 URL: {}, JWT 인증 생략", requestUri);
      filterChain.doFilter(request, response);
      return;
    }

    String token = getTokenStrFromBearer(request);
    if (token != null) {
      log.debug("요청에서 JWT 토큰 발견");
      if (jwtUtil.validateToken(token)) {
        // token -> username
        String username = jwtUtil.getUsernameFromToken(token);
        log.info("토큰에서 추출한 사용자 이름: {}", username);

        try {
          UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
          if (userDetails != null) {
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("SecurityContextHolder에 사용자 인증 설정 완료: {}", username);
          } else {
            log.warn("사용자 정보 로드 실패: {}", username);
          }
        } catch (Exception e) {
          log.error("인증 오류: {}", e.getMessage());
        }
      } else {
        log.warn("유효하지 않은 JWT 토큰");
      }
    } else {
      log.debug("요청에서 JWT 토큰 없음");
    }
    filterChain.doFilter(request, response);
  }

  private String getTokenStrFromBearer(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      log.debug("요청 헤더에서 Bearer 토큰 발견");
      return bearerToken.substring(7);
    }
    return null;
  }
}