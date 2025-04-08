package jombi.freemates.service;

import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.model.dto.RegisterResponse;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import jombi.freemates.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final MemberRepository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
    Member savedMember = memberRepository.save(Member.builder()
        .username(request.getUsername())
        .password(bCryptPasswordEncoder.encode(request.getPassword()))
        .build());

    log.info("회원가입 완료");
    return RegisterResponse.builder()
        .username(savedMember.getUsername())
        .memberId(savedMember.getMemberId())
        .build();
  }

  //TODO: 로그인 로직 (JWT 발급)
}