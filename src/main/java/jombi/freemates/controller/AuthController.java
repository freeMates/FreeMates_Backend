package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.constant.JwtTokenType;
import jombi.freemates.model.dto.LoginRequest;
import jombi.freemates.model.dto.LoginResponse;
import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.model.dto.TokenResponse;
import jombi.freemates.service.AuthService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import me.suhsaechan.suhlogger.annotation.LogMonitor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
          
          ## 반환값 (ResponseEntity<RegisterResponse>)
          - **`memberId`**: 회원 고유 코드 
          - **`username`**: 회원 ID
          - **`nickname`**: 회원 닉네입 
          - **`email`**: 회원 이메일 
          
          ## 에러코드
          - **`DUPLICATE_NICKNAME`**: 이미 존재하는 닉네임입니다.
          - **`INVALID_AGE`**: 잘못된 나이입니다.
          - 
          """
  )

  @PostMapping("/register")
  @LogMonitor
  public ResponseEntity<RegisterResponse> register(
      @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        - **맞을 시**: "true"
        - **틀릴 시**: "false"
          """)
  @GetMapping("/duplicate/username")
  @LogMonitor
  public Boolean duplicateUsername(@RequestParam String username) {
    return authService.duplicateUsername(username);
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
  @PostMapping("/login/app")
  public ResponseEntity<LoginResponse> loginApp(
      @RequestBody LoginRequest request
  ) {
    LoginResponse loginResponse = authService.login(request);
    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/login/web")
  public LoginResponse loginWeb(
      @RequestBody LoginRequest request,
      HttpServletResponse response
  ) {
    LoginResponse loginResponse = authService.login(request);

    // Set-Cookie
    response.addHeader(HttpHeaders.SET_COOKIE,
        authService.buildRefreshCookie(loginResponse.getRefreshToken()).toString());

    return LoginResponse.builder()
        .accessToken(loginResponse.getAccessToken())
        .nickname(loginResponse.getNickname())
        .build();
  }


  /**
   * 토큰 재발급
   * **/
  @PostMapping("/refresh/app")
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-27",
          author = Author.LEEDAYE,
          issueNumber = 53,
          description = "앱용 토큰 저장 로직 수정"
      )
  })
  @Operation(
      summary = "토큰 재발급",
      description = """
        ## 인증 (JWT): **불필요**

        ## 요청(String)
        - refreshToken: 기존에 있는 'String' 다 지우고 리프래시토큰 넣어야 함.

        ## 반환
        - ResponseEntity<TokenResponse>
          - `accessToken`: 새로 발급된 AccessToken
          - `refreshToken`: 새로 발급된 RefreshToken

        ## 에러 코드
        - `REFRESH_TOKEN_EXPIRED`: RefreshToken이 만료되었거나 존재하지 않습니다.
        - `ACCESS_TOKEN_EXPIRED`: AccessToken이 만료되었습니다.
    """
  )
  @LogMonitor
  public ResponseEntity<TokenResponse> refreshApp(@RequestBody String refreshToken) {
    TokenResponse tokenResponse = authService.refresh(refreshToken);
    return ResponseEntity.ok(tokenResponse);
  }



  @PostMapping("/refresh/web")
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-04-27",
          author = Author.LEEDAYE,
          issueNumber = 53,
          description = "웹용 토큰 저장 로직 수정"
      )
  })
  @Operation(
      summary = "토큰 재발급",
      description = """
        ## 인증 (JWT): **불필요**
        ## 요청
        - 별도 요청 Body 없음
        - 요청 시, 브라우저 쿠키에 저장된 RefreshToken을 자동으로 전송해야 함

        ## 반환
        - ResponseEntity<TokenResponse>
          - `accessToken`: 새로 발급된 AccessToken (Body에 포함)
          - `refreshToken`: 새로 발급된 RefreshToken (쿠키로 Set-Cookie 설정)

        ## 에러 코드
        - `REFRESH_TOKEN_EXPIRED`: RefreshToken이 만료되었거나 존재하지 않습니다.
    """
  )
  public TokenResponse refreshWeb(@CookieValue(name="refreshToken", required=false) String cookieToken,
      HttpServletResponse response) {
    if (cookieToken == null) {
      throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
    TokenResponse tokenResponse = authService.refresh(cookieToken);


    // Set-Cookie
    response.addHeader(HttpHeaders.SET_COOKIE,
        authService.buildRefreshCookie(tokenResponse.getRefreshToken()).toString());
    // Body에는 accessToken만
    return TokenResponse.builder()
        .accessToken(tokenResponse.getAccessToken())
        .build();

  }











}
