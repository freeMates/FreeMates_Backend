package jombi.freemates.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.postgres.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

  Optional<Place> findByPlaceId(UUID placeId);

  // placeName 검색
  @Query("SELECT p FROM Place p " +
      "WHERE LOWER(p.placeName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Place> searchByName(@Param("keyword") String keyword, Pageable pageable);

  // introText 검색
  @Query("SELECT p FROM Place p " +
      "WHERE LOWER(p.introText) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Place> searchByIntro(@Param("keyword") String keyword, Pageable pageable);

  // tags 검색 (ElementCollection join)
  @Query("SELECT DISTINCT p FROM Place p " +
      "JOIN p.tags t " +
      "WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Place> searchByTag(@Param("keyword") String keyword, Pageable pageable);

  // 전체(이름 OR 소개 OR 태그) 검색
  @Query("SELECT DISTINCT p FROM Place p " +
      "LEFT JOIN p.tags t " +
      "WHERE LOWER(p.placeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "   OR LOWER(p.introText) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "   OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Place> searchAllFields(@Param("keyword") String keyword, Pageable pageable);

  // 카테고리 타입으로 장소를 페이지네이션하여 조회
  Page<Place> findByCategoryType(CategoryType categoryType, Pageable pageable);

  Optional<Place> findByXAndY(String x, String y);

  @Query(
      value = "SELECT * " +
          "FROM place p " +
          "WHERE CAST(p.x AS double precision) BETWEEN :xMin AND :xMax " +
          "  AND CAST(p.y AS double precision) BETWEEN :yMin AND :yMax",
      nativeQuery = true
  )
  List<Place> findByCoordinateRange(
      @Param("xMin") double xMin,
      @Param("xMax") double xMax,
      @Param("yMin") double yMin,
      @Param("yMax") double yMax
  );

}
