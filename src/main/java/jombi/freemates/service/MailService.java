package jombi.freemates.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;
import jombi.freemates.util.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
  private final JavaMailSender mailSender;
  private static final String FROM = "${spring.mail.username}";
  private final JavaMailSender javaMailSender;
  private final RedisTemplate<String, Object> redisTemplate;

  //랜덤으로 숫자 생성
  private static String createCode() {
    return String.valueOf((int)(Math.random() * (90000)) + 100000);
  }

  //메세지 만들기
  private MimeMessage createMessage(String mail,Object code)throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    message.setFrom(new InternetAddress(FROM));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
    message.setSubject("freemates 이메일 인증");
    String body = "";
    body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
    body += "<h1>" + code + "</h1>";
    body += "<h3>" + "감사합니다." + "</h3>";
    message.setText(body,"UTF-8", "html");
    return message;

  }
  //메일 발송
  public void sendMail(String mail) {
    Object code = createCode();

    //redis에 3분간 저장
    redisTemplate.opsForValue().set(mail, code,3, TimeUnit.MINUTES);


    try{MimeMessage message = createMessage(mail, code);
    javaMailSender.send(message);}
    catch (MessagingException e) {
      log.error("메일전송이 실패하였습니다", e);
      throw new MailSendException("failed to send mail", e);
    }

  }

  //숫자 맞는지 확인
  public boolean validateCode(String mail, Object code) {
    //redis에서 코드 가져오기
    Object savedCode = redisTemplate.opsForValue().get(mail);
    return savedCode != null && savedCode.equals(code);


  }


}
