package jombi.freemates.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  @Qualifier("kakao")
  public WebClient kakaoWebClient(
      WebClient.Builder builder,
      @Value("${kakao.api.key}") String apiKey
  ) {
    return builder
        .baseUrl("https://dapi.kakao.com")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey)
        .build();
  }
}