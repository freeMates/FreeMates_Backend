package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jombi.freemates.model.constant.Author;
import jombi.freemates.service.MailService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import me.suhsaechan.suhlogger.annotation.LogMonitor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "메일 API",
    description = "메일 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {
  private final MailService mailService;


  // 이메일 전송
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-15",
          author = Author.LEEDAYE,
          issueNumber = 50,
          description = "이메일인증 기능추가"
      )
  })
  @Operation(
      summary = "메일전송",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (String)
          - **`email`**: 회원 이메일
          
          ## 반환값 (ResponseEntity<String>)
          - **`username`**: 회원 ID

          ## 에러코드
          - **`INVALID_EMAIL`**: 유효하지 않은 이메일입니다
          - **`DUPLICATE_EMAIL`**: 이미 존재하는 아이디입니다
          """
  )
  @GetMapping("/send")
  @LogMonitor
  public ResponseEntity<String> sendEmail(@RequestParam String mail) {
    mailService.sendEmail(mail);
    return ResponseEntity.ok("메일 전송을 요청하였습니다.");
  }

  // 이메일 인증
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-15",
          author = Author.LEEDAYE,
          issueNumber = 50,
          description = "이메일인증 기능추가"
      )
  })
  @Operation(
      summary = "이메일 인증",
      description = """
        ## 인증(JWT): **불필요**

        ## 설명  
        회원가입 시 전송된 이메일에 포함된 인증 링크를 클릭할 때 호출되는 API입니다.  
        이 요청을 통해 사용자의 이메일이 실제로 존재하고, 본인이 소유하고 있음을 인증합니다.

        ## 요청 파라미터 (Query)
        - **`mail`**: 이메일 주소 (예: user@example.com)
        - **`uuidString`**: 이메일에 포함된 인증 코드(UUID)

        ## 반환값 (ResponseEntity<String>)
        - **성공 시**: "인증이 완료되었습니다. 앱화면으로 돌아가 주세요"
        - **실패 시**: "인증 코드가 유효하지 않거나 만료되었습니다."

        ## 에러 상황
        - 인증 코드가 Redis에 존재하지 않거나 일치하지 않을 경우
        - 시스템 오류 발생 시

        ⚠️ 이 API는 이메일 링크를 클릭할 때 자동으로 호출되며, 사용자에게는 결과 메시지만 표시됩니다.
        """
  )
  @GetMapping("/verify")
  @LogMonitor
  public ResponseEntity<String> verifyEmail(
      @RequestParam String mail,
      @RequestParam String uuidString) {
    return ResponseEntity.ok(mailService.validateCode(mail, uuidString));
  }
}
