package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "장소 API",
    description = "장소 갱신 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/place")
//@PreAuthorize("hasRole('ROLE_ADMIN')")
//추후 권한 설정
public class PlaceController {
  private final PlaceService placeService;
  private final PlaceRepository placeRepository;
  @PostMapping("/refresh")
  public ResponseEntity<Void> refreshPlaces() {
    placeService.deleteAllAndRefresh();
    return ResponseEntity.ok().build();
  }

}
