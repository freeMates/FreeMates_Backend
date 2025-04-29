package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
          date = "2025-04-25",
          author = Author.SUHSAECHAN,
          issueNumber = 50,
          description = "이메일인증후 html 리다이렉션 코드 추가, HTML CSS 고도화"
      ),
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
          date = "2025-05-26",
          author = Author.SUHSAECHAN,
          issueNumber = 63,
          description = "이메일인증 성공/실패시 리다이렉션 html 추가 및 연결 로직 추가, 메소드명 수정, HTML CSS 고도화"
      ),
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
      프론트측에서 별도로 사용하지 않고, 사용자가 이메일에 포함된 링크를 클릭할 때 자동으로 호출됩니다. 
      이 요청을 통해 사용자의 이메일이 실제로 존재하고, 본인이 소유하고 있음을 인증합니다.

      ## 요청 파라미터 (Query)
      - **`mail`**: 이메일 주소 (예: user@example.com)
      - **`uuidString`**: 이메일에 포함된 인증 코드(UUID)

      ## 반환값
      - **성공 시**: 인증 성공 페이지로 리다이렉트 /mail/verification-confirm
      - **실패 시**: 인증 실패 페이지로 리다이렉트 /mail/verification-fail

      ## 에러 상황
      - 인증 코드가 Redis에 존재하지 않거나 일치하지 않을 경우
      - 시스템 오류 발생 시

      ⚠️ 이 API는 이메일 링크를 클릭할 때 자동으로 호출되며, 사용자에게는 결과 페이지가 표시됩니다.
      """
  )
  @GetMapping("/verify")
  @LogMonitor
  public void verifyEmailAndRedirect(
      @RequestParam String mail,
      @RequestParam String uuidString,
      HttpServletResponse response) throws IOException {

    String redirectUrl = mailService.validateCodeAndGetRedirectUrl(mail, uuidString);
    response.sendRedirect(redirectUrl);
  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-29",
          author = Author.SUHSAECHAN,
          issueNumber = 74,
          description = "이메일 인증 상태 확인 API 추가"
      )
  })
  @Operation(
      summary = "이메일 인증 상태 확인",
      description = """
    ## 인증(JWT): **불필요**
    
    ## 설명
    이메일이 인증되었는지 상태를 확인하는 API입니다.
    클라이언트에서 이메일 인증 확인 버튼을 클릭했을 때 호출
    
    ## 요청 파라미터 (Query)
    - **`mail`**: 이메일 주소 (예: test@test.com)
    
    ## 반환값
    - **성공 시**: true (인증된 이메일)
    - **실패 시**: false (인증되지 않은 이메일)
    """
  )
  @GetMapping("/check-verification")
  @LogMonitor
  public ResponseEntity<Boolean> checkEmailVerification(@RequestParam String mail) {
    return ResponseEntity.ok(mailService.isEmailVerified(mail));
  }

}
