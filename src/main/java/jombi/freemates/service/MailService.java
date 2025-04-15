package jombi.freemates.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.util.CommonUtil;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
  private final JavaMailSender mailSender;
  private static final String FROM = "${spring.mail.username}";
  private final RedisTemplate<String, Object> redisTemplate;
  private final AuthService authService;

  @Value("${freemates.host}")
  private String baseUrl;



  //메세지 만들기
  private MimeMessage createMessage(String mail,Object uuid)throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    message.setFrom(new InternetAddress(FROM));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
    message.setSubject("freemates 이메일 인증");

    String verificationUrl= baseUrl +"/api/auth/mail/verify?mail="+mail+"&uuid="+uuid;

    String body = "";
    body += "<h3> 다음을 눌러 인증을 완료해 주세요</h3>";
    body += "<button style=\"padding:10px 20px; background-color:#CCF6FF; color:#fff; border:none; border-radius:5px; cursor:pointer;\" "
        + "<p><a href='" + verificationUrl + "' style='padding:10px 20px; background-color:#007BFF; color:#fff; text-decoration:none; border-radius:5px; display:inline-block;'>인증하기</a></p>";
    message.setText(body,"UTF-8", "html");
    return message;

  }

  @Async
  //메일 발송
  public void sendEmail(String mail) {
    Object uuid = CommonUtil.creatUUID();

    //redis에 3분간 저장
    redisTemplate.opsForValue().set("verify:"+mail, uuid,3, TimeUnit.MINUTES);
    try{
      MimeMessage message = createMessage(mail, uuid);
    mailSender.send(message);}
    catch (MessagingException e) {
      log.error("메일전송이 실패하였습니다", e);
      throw new MailSendException("failed to send mail", e);
    }

  }

  //인증되었는지 확인
  public boolean validateCode(String mail, Object uuid) {
    //redis에서 코드 가져오기
    Object savedUUID = redisTemplate.opsForValue().get("verify:"+mail);
    //멤버 상태 갱신
    boolean isValid = savedUUID != null && savedUUID.equals(uuid);
    //참인경우 저장
    if (isValid) {
      // 인증 성공 시 회원가입 완료
      authService.completeRegister(mail);
      // Redis에서 인증 코드 삭제
      redisTemplate.delete("verify:" + mail);
      return true;
    } else {
      // 인증 실패 시 예외 던지기
      throw new CustomException(ErrorCode.REGISTRATION_EXPIRED);
    }

  }


}
