package jombi.freemates.repository;

import jombi.freemates.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

  Boolean existsByUsername(String username);

  UserEntity findByUsername(String username);

}
