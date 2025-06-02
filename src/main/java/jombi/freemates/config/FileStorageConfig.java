package jombi.freemates.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {
  @Bean
  public Path uploadDir(FileStorageProperties props) {
    Path path = Paths
        .get(props.getUploadDir())
        .toAbsolutePath()
        .normalize();
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new RuntimeException("업로드 디렉터리 생성 실패", e);
    }
    return path;
  }

}
