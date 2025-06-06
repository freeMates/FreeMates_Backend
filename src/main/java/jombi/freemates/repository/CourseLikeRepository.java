package jombi.freemates.repository;

import jombi.freemates.model.postgres.CourseLike;
import jombi.freemates.model.postgres.id.CourseLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseLikeRepository extends JpaRepository<CourseLike, CourseLikeId> {

  boolean existsById(CourseLikeId id);
}
