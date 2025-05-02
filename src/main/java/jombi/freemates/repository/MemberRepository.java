package jombi.freemates.repository;

import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.postgres.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
  Boolean existsByEmail(String email);

  Boolean existsByUsername(String username);
  Boolean existsByNickname(String nickname);

  Optional<Member> findByUsername(String username);
  Optional<Member> findByEmail(String email);
  Optional<Member> findByMemberId(UUID memberId);

}
