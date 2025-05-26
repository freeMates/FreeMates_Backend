package jombi.freemates.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
  @Value("${file.upload-dir}")        // application.yml 에 정의된 경로
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 클라이언트가 /uploads/** 로 요청하면, 실제 디스크의 uploadDir 경로를 뒤에 매핑
    registry
        .addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/");
  }
}
