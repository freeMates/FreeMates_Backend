package jombi.freemates.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import jombi.freemates.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {
  private final Path uploadDir;

  public String storeImage(MultipartFile file) {
    String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
    String filename = UUID.randomUUID() + "." + ext;
    Path target = uploadDir.resolve(filename);
    try {
      file.transferTo(target);
    } catch (Exception e) {
      throw new RuntimeException("파일 저장 실패", e);
    }
    // 예: "/uploads/xxxxx.jpg" 같은 URL 경로 리턴
    return "/uploads/" + filename;
  }

}
