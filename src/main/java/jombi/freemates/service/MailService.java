package jombi.freemates.service;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import java.util.concurrent.TimeUnit;
import jombi.freemates.repository.MemberRepository;
import jombi.freemates.util.CommonUtil;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
@Email
public class MailService {

  private final JavaMailSender mailSender;
  private final RedisTemplate<String, String> redisTemplate;
  private final MemberRepository memberRepository;
  private final SpringTemplateEngine templateEngine;

  // Redis 이메일 코드 유효시간 : 3분
  private static final long EMAIL_CODE_EXPIRE_MIN = 3;

  // Redis 이메일 코드 key 접두사(prefix)
  private static final String EMAIL_CODE_PREFIX = "verify:";

  // 이메일 인증 BASE URL
  @Value("${freemates.host}")
  private String baseUrl;

  // 메일 발신자
  @Value("${spring.mail.username}")
  private String from;

  // 메세지 만들기
  private MimeMessage createMessage(String mail, String uuidString) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    message.setFrom(new InternetAddress(from));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
    message.setSubject("Freemates 이메일 인증 메일입니다.");

    Context context = new Context();
    context.setVariable("mail", mail);
    context.setVariable("uuidString", uuidString);
    context.setVariable("verificationUrl", baseUrl + "/api/mail/verify?mail=" + mail + "&uuidString=" + uuidString);
    context.setVariable("baseUrl", baseUrl);

    String body = templateEngine.process("/mail/verificationEmail", context);
    message.setText(body, "UTF-8", "html");

    return message;

  }

  // 메일 발송
  @Async
  public void sendEmail(String mail) {

    // 이메일 형식 검증
    EmailValidator emailValidator = EmailValidator.getInstance();
    boolean isValidEmail = emailValidator.isValid(mail);
    if (!isValidEmail) {
      throw new CustomException(ErrorCode.INVALID_EMAIL);
    }

    // 중복 이메일 검증
    boolean isEmailExist = memberRepository.existsByEmail(mail);
    if (isEmailExist) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    // UUID 인증코드 생성
    String uuidString = CommonUtil.createCleanUuidString();

    // Redis에 인증 코드 저장 (유효시간: 3분)
    redisTemplate.opsForValue().set(EMAIL_CODE_PREFIX + mail, uuidString, EMAIL_CODE_EXPIRE_MIN, TimeUnit.MINUTES);

    // 메일 발송
    try {
      MimeMessage message = createMessage(mail, uuidString);
      mailSender.send(message);
    } catch (MessagingException e) {
      log.error("메일전송이 실패하였습니다", e);
      throw new MailSendException("failed to send mail", e);
    }

  }

  // 이메일 코드 인증 - 리다이렉션 URL 반환
  public String validateCodeAndGetRedirectUrl(String mail, String uuidString) {
    final String REDIRECT_SUCCESS_URL = "/mail/verification-confirm";
    final String REDIRECT_FAIL_URL = "/mail/verification-fail";

    try {
      // Redis에서 인증 코드 조회
      String redisKey = EMAIL_CODE_PREFIX + mail;
      String savedUuidString = redisTemplate.opsForValue().get(redisKey);

      // null 또는 "null" 문자열 처리
      savedUuidString = CommonUtil.nvl(savedUuidString, "");

      // 인증 코드 검증
      if (!savedUuidString.equals(uuidString)) {
        log.error("이메일 인증 실패: 유효하지 않은 인증 코드 (email: {}, code: {})", mail, uuidString);
        return REDIRECT_FAIL_URL;
      }

      // 인증 성공 시 Redis에서 코드 삭제
      redisTemplate.delete(redisKey);
      log.info("이메일 인증 성공: {}", mail);

      return REDIRECT_SUCCESS_URL;
    } catch (Exception e) {
      log.error("이메일 인증 중 오류 발생", e);
      return REDIRECT_FAIL_URL;
    }
  }
}
