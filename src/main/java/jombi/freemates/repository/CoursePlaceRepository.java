package jombi.freemates.repository;

import jombi.freemates.model.postgres.Course;
import jombi.freemates.model.postgres.CoursePlace;
import jombi.freemates.model.postgres.id.CoursePlaceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, CoursePlaceId> {
  void deleteAllByCourse(Course course);

}
