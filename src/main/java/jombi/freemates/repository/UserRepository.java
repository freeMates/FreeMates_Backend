package jombi.freemates.repository;

import jombi.freemates.model.postgres.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

  Boolean existsByUsername(String username);

  UserEntity findByUsername(String username);

}
