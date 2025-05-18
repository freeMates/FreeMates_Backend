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
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // username (로그인 ID)
      Member member = memberRepository.findByUsername(username)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      return new CustomUserDetails(member);
  }

}
