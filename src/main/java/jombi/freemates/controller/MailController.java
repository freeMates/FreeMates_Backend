package jombi.freemates.controller;

import jombi.freemates.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {
  private final MailService mailService;


  //이메일 전송
  @GetMapping("send")
  public ResponseEntity<String> sendEmail(@RequestParam String mail) {
    mailService.sendEmail(mail);
    return ResponseEntity.ok("메일 전송을 요청하였습니다.");
  }

  //이메일 인증
  @GetMapping("verify")
  public ResponseEntity<String> verifyEmail(@RequestParam String mail,@RequestParam Object uuidObject) {

    try{
      mailService.validateCode(mail, uuidObject);
      return ResponseEntity.ok("인증이 완료되었습니다. 앱화면으로 돌아가 주세요");

    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("오류가 발생하였습니다: " + e.getMessage());

    }

  }

}
