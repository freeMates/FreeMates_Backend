package jombi.freemates.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.lineLogDebug;
import static me.suhsaechan.suhlogger.util.SuhLogger.superLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.superLogDebug;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;
import static org.junit.jupiter.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhlogger.util.SuhLogger;
import me.suhsaechan.suhnicknamegenerator.core.SuhRandomKit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class CommonUtilTest {
  private SuhRandomKit suhRandomKit = SuhRandomKit.builder().numberLength(4).uuidLength(4).enableAdultContent(true).build();

  @Test
  public void mainTest() {
    lineLog("테스트 시작");

    lineLog(null);
    timeLog(this::normalNickname_테스트);
    lineLog(null);

    lineLog(null);
    timeLog(this::matureNickname_테스트);
    lineLog(null);

    lineLog("테스트 종료");
  }

  public void normalNickname_테스트(){
    lineLogDebug("normalNickname_테스트");
    String simpleNickname = suhRandomKit.simpleNickname();
    superLog(simpleNickname);
    String nicknameWithNumber = suhRandomKit.nicknameWithNumber();
    superLog(nicknameWithNumber);
    String nicknameWithUuid = suhRandomKit.nicknameWithUuid();
    superLog(nicknameWithUuid);
  }

  public void matureNickname_테스트(){
    superLog("matureNickname_테스트");
    String matureNickname = suhRandomKit.matureNickname();
    superLog(matureNickname);
    String matureNicknameWithNumber = suhRandomKit.matureNicknameWithNumber();
    superLog(matureNicknameWithNumber);
    String matureNicknameWithUuid = suhRandomKit.matureNicknameWithUuid();
    superLog(matureNicknameWithUuid);
  }


}