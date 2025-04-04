package jombi.freemates.util.object;

import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.constant.HashType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashRegistryRepository extends JpaRepository<HashRegistry, UUID> {
  Optional<HashRegistry> findByHashType(HashType hashType);
}
