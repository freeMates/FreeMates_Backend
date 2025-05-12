package jombi.freemates.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.UUID;
import jombi.freemates.model.constant.JwtTokenType;
import jombi.freemates.model.constant.Role;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.dto.LoginRequest;
import jombi.freemates.model.dto.LoginResponse;
import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.model.dto.TokenRequest;
import jombi.freemates.model.dto.TokenResponse;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.model.postgres.RefreshToken;
import jombi.freemates.repository.RefreshTokenRepository;
import jombi.freemates.util.JwtUtil;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import jombi.freemates.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhlogger.util.SuhLogger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

  private final CustomUserDetailsService customUserDetailsService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final MemberRepository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;
  // 회원가입 나이 제한
  private static final Integer MIN_AGE = 18;
  private static final Integer MAX_AGE = 90;

  /**
   * 회원가입
   */
  public RegisterResponse register(RegisterRequest request) {
    int age = request.getAge();// 한국식 나이
    int currentYear = LocalDate.now().getYear();
    int birthYear = currentYear - age + 1;

    // 중복 닉네임 검증
    if (memberRepository.existsByNickname(request.getNickname())) {
      log.error("이미 사용중인 닉네임입니다. 요청 아이디: {}", request.getNickname());
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    // 정상나이 확인
    if (age < MIN_AGE || age > MAX_AGE) {
      log.error("정상적인 나이 범위가 아닙니다 {}", age);
      throw new CustomException(ErrorCode.INVALID_AGE);
    }
    try {
      // 사용자 저장
      Member savedMember = memberRepository.save(
          Member.builder()
              .username(request.getUsername())
              .password(bCryptPasswordEncoder.encode(request.getPassword()))
              .email(request.getEmail())
              .birthYear(birthYear)
              .gender(request.getGender())
              .nickname(request.getNickname())
              .role(Role.ROLE_USER)
              .isDeleted(false)
              .build());

      SuhLogger.superLog(savedMember);
      return RegisterResponse.builder()
          .username(savedMember.getUsername())
          .memberId(savedMember.getMemberId())
          .email(savedMember.getEmail())
          .nickname(savedMember.getNickname())
          .build();
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);

    }
  }


  /**
   * 아이디 중복 확인
   */
  public boolean duplicateUsername(String username) {

    try {
      return memberRepository.existsByUsername(username);
    } catch (RuntimeException e) {
      return false;
    }
  }

  /**
   * 로그인
   */
  public LoginResponse login(LoginRequest request) {
    // Authentication 생성
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    // Member 추출
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Member member = userDetails.getMember();

    // 토큰 생성
    // AccessToken 발급
    String accessToken = jwtUtil.generateToken(authentication, JwtTokenType.ACCESS);
    // RefreshToken 발급
    String refreshToken = jwtUtil.generateToken(authentication, JwtTokenType.REFRESH);

    refreshTokenRepository.save(
        RefreshToken.builder()
            .refreshToken(refreshToken)
            .member(member)
            .build()
    );

    //토큰 반환
    return LoginResponse.builder()
        .refreshToken(refreshToken)
        .accessToken(accessToken)
        .nickname(member.getNickname())
        .build();
  }

  /**
   * 리프레시 토큰 재발급
   */
  public TokenResponse refresh(TokenRequest tokenRequest) {
    // refreshToken 유효성 검증
    String refreshToken = tokenRequest.getRefreshToken();
    if (!jwtUtil.validateToken(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // refreshToken에서 username 추출
    String username = jwtUtil.getUsernameFromToken(refreshToken);

    Member member = memberRepository.findByUsername(username)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 저장된 RefreshToken과 비교
    RefreshToken savedToken = refreshTokenRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED));
    if (!savedToken.getRefreshToken().equals(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    CustomUserDetails userDetails = new CustomUserDetails(member);

    // 새 토큰 발급
    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities()
    );
    String newAccessToken = jwtUtil.generateToken(auth, JwtTokenType.ACCESS);
    String newRefreshToken = jwtUtil.generateToken(auth, JwtTokenType.REFRESH);

    // RefreshToken 업데이트
    savedToken.setRefreshToken(newRefreshToken);
    refreshTokenRepository.save(savedToken);

    // 새 AccessToken + 새 RefreshToken 반환
    return TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  /**
   * refresh 토큰 쿠키 생성
   */
  public ResponseCookie buildRefreshCookie(String token) {
    return ResponseCookie.from("refreshToken", token)
        .httpOnly(true).secure(true)
        .path("/api/auth/refresh/web")
        .maxAge(JwtTokenType.REFRESH.getDurationMilliseconds() / 1000)
        .sameSite("Strict")
        .build();
  }

  /**
   * 회원탈퇴(hard와 soft를 나눠서 탈퇴)
   */
  public void delete(Member member, boolean hard) {
    if (hard) {
      // 연관된 RefreshToken도 제거
      refreshTokenRepository.deleteByMember(member);
      memberRepository.delete(member);  // 하드 딜리트
    } else {
      if (member.isDeleted()) {
        throw new CustomException(ErrorCode.ALREADY_DELETED);
      }
      member.markDeleted();  // 소프트 딜리트 (isDeleted = true로 설정)
      memberRepository.save(member);
    }
  }

}