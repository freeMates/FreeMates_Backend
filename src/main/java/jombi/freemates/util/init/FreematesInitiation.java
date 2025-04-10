package jombi.freemates.util.init;

import java.time.LocalDateTime;
import jombi.freemates.util.LogUtil;
import jombi.freemates.util.docs.GithubIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreematesInitiation implements ApplicationRunner {

  private final GithubIssueService githubIssueService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    LogUtil.lineLog("SERVER START");
    LogUtil.lineLog("데이터 초기화 시작");
    LocalDateTime startTime = LocalDateTime.now();

    githubIssueService.syncGithubIssues();

    LogUtil.logServerInitDuration(startTime);
    log.info("서버 데이터 초기화 및 업데이트 완료");
  }
}
