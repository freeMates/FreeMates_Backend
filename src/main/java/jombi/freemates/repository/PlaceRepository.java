package jombi.freemates.repository;

import jombi.freemates.model.postgres.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, String> {

}
