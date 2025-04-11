package jombi.freemates.service;

import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.repository.MemberRepository;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // AuthenticationManager -> UserDetails 검증
    return new CustomUserDetails(
        memberRepository.findByUsername(username).orElseThrow(()
            -> new CustomException(ErrorCode.DUPLICATE_USERNAME)));
  }
}
