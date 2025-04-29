package jombi.freemates.service;

import java.time.LocalDate;
import jombi.freemates.model.constant.JwtTokenType;
import jombi.freemates.model.constant.Role;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.dto.LoginRequest;
import jombi.freemates.model.dto.LoginResponse;
import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.util.JwtUtil;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import jombi.freemates.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhlogger.util.SuhLogger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

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
  public RegisterResponse register(RegisterRequest request){
    int age = request.getAge();// 한국식 나이
    int currentYear = LocalDate.now().getYear();
    int birthYear = currentYear - age+ 1;

    // 중복 닉네임 검증
    if(memberRepository.existsByNickname(request.getNickname())){
      log.error("이미 사용중인 닉네임입니다. 요청 아이디: {}",request.getNickname());
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    // 정상나이 확인
    if(age<MIN_AGE || age>MAX_AGE){
      log.error("정상적인 나이 범위가 아닙니다 {}",age);
      throw new CustomException(ErrorCode.INVALID_AGE);
    }
    try{
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
            .build());

    SuhLogger.superLog(savedMember);
    return RegisterResponse.builder()
          .username(savedMember.getUsername())
          .memberId(savedMember.getMemberId())
          .email(savedMember.getEmail())
          .nickname(savedMember.getNickname())
          .build();}
    catch(Exception e){
      throw new CustomException(ErrorCode.INVALID_REQUEST);

    }
  }


  /**
   * 아이디 중복 확인
   */
  public boolean duplicateUsername(String username) {
    try{
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
        new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword())
    );

    // Member 추출
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Member member = userDetails.getMember();

    //토큰 생성
    // AccessToken 발급
    String accessToken = jwtUtil.generateToken(authentication, JwtTokenType.ACCESS);
    // RefreshToken 발급
    String refreshToken = jwtUtil.generateToken(authentication, JwtTokenType.REFRESH);

    //토큰 반환
    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .nickname(member.getNickname())
        .build();
  }

}