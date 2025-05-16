package jombi.freemates.config;

import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOriginPatterns(Collections.singletonList("*")); // 모든 Origin 허용

    // 모든 HTTP Method 허용 (GET, POST, PUT, DELETE 등)
    config.setAllowedMethods(List.of("*"));

    // 모든 HTTP Header 허용
    config.setAllowedHeaders(List.of("*"));

    // 자격 증명 허용 (쿠키, Authorization 헤더 포함)
    config.setAllowCredentials(true);

    // Pre-flight 요청 캐싱 시간(1시간)
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
