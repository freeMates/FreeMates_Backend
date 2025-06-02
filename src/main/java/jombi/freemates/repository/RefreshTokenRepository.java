package jombi.freemates.repository;

import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.model.postgres.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
 Optional<RefreshToken> findByMember(Member member);
 Optional<RefreshToken> findByMember_MemberId(UUID memberId);
 void deleteByMember(Member member);

}
