package jombi.freemates.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jombi.freemates.config.SecurityUrls;
import jombi.freemates.dto.CustomUserDetails;
import jombi.freemates.entity.UserEntity;
import jombi.freemates.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 인증 생략 경로
        if (isWhitelistedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.error("토큰이 존재하지 않거나 형식이 잘못되었습니다.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        log.info("토큰이 존재합니다");

        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {

            log.error("JWT토큰이 유효하지 않습니다.");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        // 토큰에서 username 획득
        String username = jwtUtil.getUsername(token);

        //user를 생성하여 값 set
        UserEntity user = UserEntity.builder()
                .username(username)
                .build();

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    /**
     * 화이트리스트 경로 확인 (인증x)
     *
     * @param uri 요청된 URI
     * @return 화이트리스트 여부
     */
    private boolean isWhitelistedPath(String uri) {
        return SecurityUrls.AUTH_WHITELIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
