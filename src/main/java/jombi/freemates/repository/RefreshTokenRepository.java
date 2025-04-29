package jombi.freemates.repository;

import java.util.Optional;
import jombi.freemates.model.postgres.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
 Optional<RefreshToken> findByUsername(String username);

}
