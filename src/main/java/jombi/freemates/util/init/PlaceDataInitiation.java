package jombi.freemates.util.init;

import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceDataInitiation{
  private final PlaceService placeService;
  private final PlaceRepository placeRepository;

  /**
   *
   * DB가 비어 있을 때나 관리자가 호출 시 kakao API 를 호출해 저장
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onAppReady() {
    log.debug("장소 개수 {}", placeRepository.count());
    placeService.refreshPlacesIfEmpty();
  }




}

