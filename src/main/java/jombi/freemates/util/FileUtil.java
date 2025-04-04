package jombi.freemates.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

  /**
   * 파일 명 생성
   *
   * @param originalFilename 원본 파일 명
   * @return ex) 1711326597434_fileName.png
   */
  public static String generateFilename(String originalFilename) {
    String timeStamp = String.valueOf(System.currentTimeMillis());
    String fileName = timeStamp + "_" + originalFilename;
    log.debug("파일 명 생성: {}", fileName);
    return fileName;
  }

  /**
   * SMB root-dir, dir에 따른 파일 경로 생성
   */
  public static String generateSmbFilePath(String... parts) {
    return "/" + String.join("/", parts);
  }
}
