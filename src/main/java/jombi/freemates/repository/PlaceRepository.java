package jombi.freemates.repository;

import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.postgres.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

  Optional<Place> findByPlaceId(UUID placeId);

}
