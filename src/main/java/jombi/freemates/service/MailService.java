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

@Slf4j
@Service
@RequiredArgsConstructor
@Email
public class MailService {

  private final JavaMailSender mailSender;
  private final RedisTemplate<String, String> redisTemplate;
  private final AuthService authService;
  private final MemberRepository memberRepository;

  // Redis 이메일 코드 유효시간 : 3분
  private static final long EMAIL_CODE_EXPIRE_MIN = 3;

  // Redis 이메일 코드 key 접두사(prefix)
  private static final String EMAIL_CODE_PREFIX = "verify:";

  // 이메일 인증 BASE URL
  @Value("${freemates.host}")
  private String baseUrl;

  // 메일 발신자
  @Value("${mail.username}")
  private String from;

  //메세지 만들기
  private MimeMessage createMessage(String mail, Object uuidObject) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    message.setFrom(new InternetAddress(from));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
    message.setSubject("freemates 이메일 인증");

    String verificationUrl = baseUrl + "/api/mail/verify?mail=" + mail + "&uuidObject=" + uuidObject;

    String body = "";
    body += "<h3> 다음을 눌러 인증을 완료해 주세요</h3>";
    body +=
        "<button style=\"padding:10px 20px; background-color:#CCF6FF; color:#fff; border:none; border-radius:5px; cursor:pointer;\" "
            + "<p><a href='" + verificationUrl
            + "' style='padding:10px 20px; background-color:#007BFF; color:#fff; text-decoration:none; border-radius:5px; display:inline-block;'>인증하기</a></p>";
    message.setText(body, "UTF-8", "html");
    return message;

  }

  // 메일 발송
  @Async
  public void sendEmail(String mail) {

    // 이메일 형식 검증
    EmailValidator validator = EmailValidator.getInstance();
    boolean isValid = validator.isValid(mail);
    if (!isValid) {
      throw new CustomException(ErrorCode.INVALID_EMAIL);
    }

    boolean isExist = memberRepository.existsByEmail(mail);
    if (isExist) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    String uuidObject = CommonUtil.createCleanUUIDString();

    //redis에 3분간 저장
    redisTemplate.opsForValue().set("verify:" + mail, uuidObject, EMAIL_CODE_EXPIRE_MIN, TimeUnit.MINUTES);
    try {
      MimeMessage message = createMessage(mail, uuidObject);
      mailSender.send(message);
    } catch (MessagingException e) {
      log.error("메일전송이 실패하였습니다", e);
      throw new MailSendException("failed to send mail", e);
    }

  }

  // 이메일 코드 인증
  public String validateCode(String mail, String uuidString) {

    final String DEFAULT_FAIL_MESSAGE = "인증 코드가 유효하지 않거나 만료되었습니다.";
    final String DEFAULT_SUCCESS_MESSAGE = "인증이 완료되었습니다. 앱화면으로 돌아가 주세요";

    try {
      // UUIDString이 null이 아닌지 확인
      if (uuidString == null || uuidString.isEmpty()) {
        throw new CustomException(ErrorCode.INVALID_REQUEST);
      }

      // Redis에서 인증 코드 가져오기
      String savedUuidString = String.valueOf(redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + mail));

      // 멤버 상태 갱신
      boolean isValidEmailCode = savedUuidString != null && savedUuidString.equals(uuidString);

      if (isValidEmailCode) {
        // Redis에서 인증 코드 삭제
        redisTemplate.delete(EMAIL_CODE_PREFIX + mail);
      } else {
        // 인증 실패 시 예외 던지기
        log.error("인증 코드가 유효하지 않거나 만료되었습니다.");
        return DEFAULT_FAIL_MESSAGE;
      }

      // 성공 메시지 반환
      return DEFAULT_SUCCESS_MESSAGE;

    } catch (Exception e) {
      log.error("메일 인증에 실패하였습니다", e);
      return DEFAULT_FAIL_MESSAGE;
    }
  }
}
