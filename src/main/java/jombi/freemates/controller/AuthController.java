package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.dto.LoginRequest;
import jombi.freemates.model.dto.LoginResponse;
import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.service.AuthService;
import jombi.freemates.util.aspect.LogMethodInvocation;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @ApiChangeLogs({
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
          
          ## 반환값 (RegisterResponse)
          - **`username`**: 회원 ID
          - **`memberId`**: 회원 고유 ID
          
          ## 에러코드
          - **`DUPLICATE_USERNAME`**: 이미 사용중인 아이디입니다.
          """
  )

  @PostMapping("/register")
  @LogMethodInvocation
  public ResponseEntity<RegisterResponse> register(
      @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-13",
          author = Author.LEEDAYE,
          issueNumber = 42,
          description = "회원가입시 받을 정보 추가현"
      ),
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
          - **`nickname`**: 회원 닉네입
          - **`email`**: 회원 이메일
          - **`gender`**: 회원 성별
          - **`birthYear`**: 회원 태어난 년도
          
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


}
