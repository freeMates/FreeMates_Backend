package jombi.freemates.repository;

import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.postgres.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

  Optional<Place> findByPlaceId(UUID placeId);

  // 카테고리 타입으로 장소를 페이지네이션하여 조회
  Page<Place> findByCategoryType(CategoryType categoryType, Pageable pageable);


}
