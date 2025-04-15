package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jombi.freemates.service.MailService;
import jombi.freemates.util.aspect.LogMethodInvocation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
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
  //TODO: DOCS
  @GetMapping("/send")
  @LogMethodInvocation
  public ResponseEntity<String> sendEmail(@RequestParam String mail) {
    mailService.sendEmail(mail);
    return ResponseEntity.ok("메일 전송을 요청하였습니다.");
  }

  // 이메일 인증
  //TODO: DOCS
  @GetMapping("/verify")
  @LogMethodInvocation
  public ResponseEntity<String> verifyEmail(
      @RequestParam String mail,
      @RequestParam String uuidString) {
    return ResponseEntity.ok(mailService.validateCode(mail, uuidString));
  }
}
