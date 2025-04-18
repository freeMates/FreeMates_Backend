package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.dto.LoginRequest;
import jombi.freemates.model.dto.LoginResponse;
import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.service.AuthService;
import jombi.freemates.service.MailService;
import jombi.freemates.util.aspect.LogMethodInvocation;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@Tag(
    name = "회원가입, 로그인 API",
    description = "회원가입, 로그인 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  /**
   * 회원가입
   * **/
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-17",
          author = Author.LEEDAYE,
          issueNumber = 62,
          description = "회원가입 API수정 및 추가"
      ),
      @ApiChangeLog(
          date = "2025-04-15",
          author = Author.LEEDAYE,
          issueNumber = 50,
          description = "이메일인증 기능추가"
      ),
      @ApiChangeLog(
          date = "2025-04-13",
          author = Author.LEEDAYE,
          issueNumber = 42,
          description = "회원가입시 받을 정보 추가"
      ),
      @ApiChangeLog(
          date = "2025-04-08",
          author = Author.SUHSAECHAN,
          issueNumber = 32,
          description = "요청객체, 반환객체 개선 및 로그 추가"
      ),
      @ApiChangeLog(
          date = "2025-04-01",
          author = Author.LEEDAYE,
          issueNumber = 12,
          description = "회원가입 API 추가"
      )
  })
  @Operation(
      summary = "회원가입",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (RegisterRequest)
          - **`username`**: 회원 ID
          - **`password`**: 회원 비밀번호 
          - **`nickname`**: 회원 닉네입 
          - **`email`**: 회원 이메일 
          - **`gender`**: 회원 성별 (gender 로 MALE, FEMALE 로 받아야함)
          - **`age`**: 나이 (18세 이상 90세 이하)
          
          ## 반환값 (ResponseEntity<String>)
        - **성공 시**: "회원가입이 완료되었습니다."

          ## 에러코드
          - **`DUPLICATE_NICKNAME`**: 이미 존재하는 닉네임입니다.
          - **`INVALID_AGE`**: 잘못된 나이입니다.
          """
  )

  @PostMapping("/register")
  @LogMethodInvocation
  public ResponseEntity<String> register(
      @RequestBody RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.ok( "회원가입이 완료되었습니다.");
  }

  /**
   * 아이디 중복 확인
   * **/
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-17",
          author = Author.LEEDAYE,
          issueNumber = 62,
          description = "아이디 중복확인"
      )})
  @Operation(summary = "아이디 중복확인",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (String)
          - **`username`**: 회원 ID
          
          ## 반환값 (ResponseEntity<String>)
        - **성공 시**: "사용 가능한 아이디입니다."
          ## 에러코드
          - **`DUPLICATE_USERNAME`**: 이미 존재하는 아이디입니다.
          """)
  @GetMapping("/duplicate/username")
  public ResponseEntity<String> duplicateUsername(@RequestParam String username) {
    authService.duplicateUsername(username);
    return ResponseEntity.ok( "사용 가능한 아이디입니다.");
  }


  /**
   * 로그인
   * **/
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-11",
          author = Author.LEEDAYE,
          issueNumber = 37,
          description = "로그인 API 구현"
      ),
      @ApiChangeLog(
          date = "2025-04-08",
          author = Author.SUHSAECHAN,
          issueNumber = 32,
          description = "요청객체, 반환객체 개선, TODO: Service 로직 추가"
      ),
      @ApiChangeLog(
          date = "2025-04-01",
          author = Author.LEEDAYE,
          issueNumber = 12,
          description = "로그인 API 추가"
      )
  })
  @Operation(
      summary = "로그인",
      description = """
          ## 인증(JWT): **불필요**
          
          ## 요청 파라미터 (LoginRequest)
          - **`username`**: 회원 ID
          - **`password`**: 회원 비밀번호

          ## 반환값 (LoginResponse)
          - **`accessToken`**: 발급된 AccessToken
          - **`refreshToken`**: 발급된 RefreshToken
          - **`nickname`**: 회원 닉네임
          
          ## 에러코드
          - **`DUPLICATE_USERNAME`**: 이미 사용중인 아이디입니다.
          - **`INVALID_CREDENTIALS`**: 유효하지 않은 자격 증명입니다.
          - **`MEMBER_NOT_FOUND`**: 회원 정보를 찾을 수 없습니다.
          """
  )
  @PostMapping("/login")
  @LogMethodInvocation
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  //아이디찾기

  //비밀번호 변경

  // 로그아웃







}
