package jombi.freemates.service;

import java.io.IOException;
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
    // file 체크
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 없습니다.");
    }

    // 원본 파일명 정리
    String originalName = StringUtils.cleanPath(file.getOriginalFilename());
    String ext = StringUtils.getFilenameExtension(originalName);
    if (ext == null || ext.isBlank()) {
      throw new IllegalArgumentException("지원하지 않는 확장자입니다: " + originalName);
    }
    ext = ext.toLowerCase(); // 확장자를 소문자로

    // UUID 기반 유니크 파일명 생성
    String filename = UUID.randomUUID().toString() + "." + ext;

    // 업로드 디렉토리 확인/생성
    try {
      if (Files.notExists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }
    } catch (IOException e) {
      throw new RuntimeException("업로드 디렉토리 생성 실패", e);
    }

    // 실제 저장할 경로 계산
    Path target = uploadDir.resolve(filename).normalize().toAbsolutePath();

    // 파일 저장
    try {
      file.transferTo(target);
    } catch (Exception e) {
      throw new RuntimeException("파일 저장 실패", e);
    }

    // 외부에서 접근 가능한 URL 경로 리턴
    return "/uploads/" + filename;
  }

}
