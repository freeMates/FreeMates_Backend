package jombi.freemates.model.postgres;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jombi.freemates.model.postgres.id.BookmarkPlaceId;
import jombi.freemates.model.postgres.id.CoursePlaceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePlace {

  @EmbeddedId
  private CoursePlaceId coursePlaceId;

  private Integer sequence;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("courseId")
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("placeId")
  @JoinColumn(name = "place_id", nullable = false)
  private Place place;


}
