package jombi.freemates.repository;

import java.util.Optional;
import jombi.freemates.model.postgres.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {

  Boolean existsByUsername(String username);

  Optional<Member> findByUsername(String username);

}
