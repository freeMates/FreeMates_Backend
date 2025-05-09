package jombi.freemates.service;

import java.util.UUID;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.repository.MemberRepository;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String idOrUsername) throws UsernameNotFoundException {
    // UUID 파싱 시도: 토큰 인증 경로
    try {
      UUID memberId = UUID.fromString(idOrUsername);
      Member member = memberRepository.findByMemberId(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

      return new CustomUserDetails(member);
    } catch (IllegalArgumentException e) {
      // idOrUsername이 UUID 포맷이 아니면 로그인 경로(username)
      Member member = memberRepository.findByUsername(idOrUsername)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      return new CustomUserDetails(member);
    }
  }

}
