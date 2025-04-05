package jombi.freemates.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@OpenAPIDefinition(
    info = @Info(
        title = "FreeMate Server",
        description = """
            FreeMate Server API 문서
            """,
        version = "1.0v"
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "로컬 서버"),
        @Server(url = "http://3.34.78.124:8087", description = "FreeMates 서버"), //FIXME: 수정 필요
        @Server(url = "http://suh-project.synology.me:8092", description = "SUH-PROJECT - FreeMates 서버")
    }
)
@Configuration
public class SwaggerConfig implements WebMvcConfigurer{

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme apiKey = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .in(SecurityScheme.In.HEADER)
        .name("Authorization")
        .scheme("bearer")
        .bearerFormat("JWT");

    SecurityRequirement securityRequirement = new SecurityRequirement()
        .addList("Bearer Token");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
        .addSecurityItem(securityRequirement)
        .servers(List.of(
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://localhost:8080")
                    .description("로컬 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://3.34.78.124:8087") //FIXME: 수정 필요
                    .description("FreeMates 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://suh-project.synology.me:8092")
                    .description("SUH-PROJECT - FreeMates 서버")
            )
        );
  }


  // Swagger UI 리소스 핸들러 등록
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/docs/swagger**")
        .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
    registry.addResourceHandler("/docs/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}