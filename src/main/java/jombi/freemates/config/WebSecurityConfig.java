package jombi.freemates.config;

import static org.springframework.security.config.Customizer.withDefaults;

import jombi.freemates.service.CustomUserDetailsService;
import jombi.freemates.util.filter.CustomAuthenticationEntryPoint;
import jombi.freemates.util.JwtUtil;
import jombi.freemates.util.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;

  public WebSecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
    this.jwtUtil = jwtUtil;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .cors(withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
        .authorizeHttpRequests(auth -> auth
            // 허용 URL
            .requestMatchers(SecurityUrls.AUTH_WHITELIST.toArray(new String[0])).permitAll()
            // 업로드된 이미지 파일은 모두에게 허용
            .requestMatchers("/uploads/**").permitAll()
            // 관리자 URL
            .requestMatchers(SecurityUrls.ADMIN_PATHS.toArray(new String[0])).hasRole("ADMIN")
            // 회원 관련 예시 URL
            .requestMatchers(HttpMethod.POST, "/api/member/my-page").hasAnyRole("ADMIN", "USER")
            .requestMatchers(HttpMethod.POST, "/api/plane/**").hasAnyRole("ADMIN", "USER")
            .anyRequest().authenticated()
        );


    // JWT 인증 필터 추가
    http.addFilterBefore(
        new JwtAuthenticationFilter(jwtUtil, customUserDetailsService),
        UsernamePasswordAuthenticationFilter.class
    );

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
