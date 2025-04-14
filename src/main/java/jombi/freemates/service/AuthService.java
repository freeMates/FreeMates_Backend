package jombi.freemates.service;

import static jombi.freemates.util.LogUtil.superLogDebug;

import jombi.freemates.model.constant.JwtTokenType;
import jombi.freemates.model.constant.Role;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.dto.LoginRequest;
import jombi.freemates.model.dto.LoginResponse;
import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.util.JwtUtil;
import jombi.freemates.util.LogUtil;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import jombi.freemates.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final MemberRepository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;


  /**
   * 회원가입
   */
  public RegisterResponse register(RegisterRequest request) {

    // 중복 아이디 검증
    if (memberRepository.existsByUsername(request.getUsername())) {
      log.error("이미 사용중인 아이디 입니다. 요청 아이디: {}", request.getUsername());
      throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
    }

    // 사용자 저장
    Member savedMember = memberRepository.save(
        Member.builder()
            .username(request.getUsername())
            .password(bCryptPasswordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .birthYear(request.getBirthYear())
            .gender(request.getGender())
            .nickname(request.getNickname())
            .role(Role.ROLE_USER)
            .build());

    superLogDebug(savedMember);
    return RegisterResponse.builder().username(savedMember.getUsername()).memberId(savedMember.getMemberId()).build();
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
        .nickname(member.getUsername())
        .build();
  }


}