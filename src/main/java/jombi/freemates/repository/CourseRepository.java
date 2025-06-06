package jombi.freemates.repository;

import java.util.List;
import java.util.UUID;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.postgres.Course;
import jombi.freemates.model.postgres.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
  List<Course> findAllByMember(Member member);
  Page<Course> findAllByVisibility(Visibility visibility, Pageable pageable);

}
