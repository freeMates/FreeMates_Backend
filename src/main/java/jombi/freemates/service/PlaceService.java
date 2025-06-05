package jombi.freemates.service;


import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.dto.KakaoPlaceCrawlDetail;
import jombi.freemates.model.dto.GeoCodePlaceDto;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.service.crawler.KakaoCrawler;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jombi.freemates.model.dto.KakaoPlaceDocumentResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
  private final PlaceRepository placeRepository;
  private final KakaoCrawler kakaoCrawler;
  private final PlaceFetchService placeFetchService;

  /**
   * 실제 DB 동기화: 삭제 여부만 갈라서, fetch→저장은 한 번에!
   */
  @Transactional
  public void refreshPlaces(boolean deleteFirst) {
    if (deleteFirst) {
      placeRepository.deleteAll();
      log.info("기존 장소 전부 삭제");
    }

    List<KakaoPlaceDocumentResponse> docs = placeFetchService.fetchPlaces()
        .timeout(Duration.ofMinutes(2))
        .block();  // 블로킹은 한 번만!

    List<Place> places = placeFetchService.buildPlaces(docs);
    log.info("총 {}개 장소 저장 시작", places.size());
    try {
      placeRepository.saveAll(places);
      log.info("장소 데이터 저장 완료");
    } catch (Exception e) {
      log.error("장소 데이터 저장 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }

      addKakaoCrawlInfo();

  }

  /**
   * 앱 구동 시, DB가 비어 있으면 한 번만 비동기 실행
   */
  @Async("applicationTaskExecutor")
  public void refreshPlacesIfEmpty() {
    if (placeRepository.count() == 0) {
      refreshPlaces(false);
    }
  }

  /**
   * 관리자 호출용: 무조건 삭제 후 다시 저장
   */
  public void deleteAllAndRefresh() {
    refreshPlaces(true);
  }


  /**
   * 카카오 크롤링 동기처리
   */

  @Transactional
  public void addKakaoCrawlInfo(){
    List<Place> places = placeRepository.findAll();
    for (Place place : places) {
      try {
        // 카카오 크롤링

        KakaoPlaceCrawlDetail kakaoPlaceCrawlDetail = kakaoCrawler.crawlByPlaceId(place.getKakaoPlaceId());
        // Place 엔티티에 정보 업데이트
        place.setImageUrl(kakaoPlaceCrawlDetail.getImageUrl());
        place.setTags(kakaoPlaceCrawlDetail.getTags());
        place.setIntroText(kakaoPlaceCrawlDetail.getIntroText());
        placeRepository.save(place);
        log.debug("이미지 저장된 거 {} 카카오에서 크롤링한거{}", place.getImageUrl(), kakaoPlaceCrawlDetail.getImageUrl());
      } catch (Exception e) {
        log.error("카카오 크롤링 중 오류 발생: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * 카테고리별 장소 조회
   */
  @Transactional(readOnly = true)
  public Page<PlaceDto> getPlacesByCategory(CategoryType category, Pageable pageable) {
    if (category == null) {
      return placeRepository.findAll(pageable)
          .map(p -> {
            PlaceDto dto = new PlaceDto(
                p.getPlaceId(),
                p.getPlaceName(),
                p.getIntroText(),
                p.getAddressName(),
                p.getImageUrl(),
                p.getTags(),
                p.getCategoryType(),
                p.getLikeCount(),
                p.getViewCount()
            );
            log.debug("카테고리 없이 장소 조회: {}", dto);
            return dto;
          });
    }

    // category가 지정된 경우: findByCategoryType 호출 후 DTO로 변환
    return placeRepository.findByCategoryType(category, pageable)
        .map(p -> {
          PlaceDto dto = new PlaceDto(
              p.getPlaceId(),
              p.getPlaceName(),
              p.getIntroText(),
              p.getAddressName(),
              p.getImageUrl(),
              p.getTags(),
              p.getCategoryType(),
              p.getLikeCount(),
              p.getViewCount()
          );
          log.debug("카테고리 [{}] 로 장소 조회: {}", category, dto);
          return dto;
        });
  }

  /**
   * 좌표에 따른 장소 조회
   */
  @Transactional(readOnly = true)
  public GeoCodePlaceDto getPlacesByGeocode(
      String x,
      String y
  ) {
    if (x == null || x.trim().isEmpty() || y == null || y.trim().isEmpty()) {
           throw new CustomException(ErrorCode.INVALID_REQUEST);
         }
    Optional<Place> placeOpt = placeRepository.findByXAndY(x, y);
    if (placeOpt.isEmpty()) {
      log.warn("좌표 ({}, {})에 해당하는 장소가 없습니다.", x, y);
      throw new CustomException(ErrorCode.PLACE_NOT_FOUND); // 또는 예외 처리
    }
    Place place = placeOpt.get();
    GeoCodePlaceDto geoCodePlaceDto = new GeoCodePlaceDto(
        place.getPlaceName(),
        place.getRoadAddressName(),
        place.getImageUrl(),
        place.getIntroText(),
        place.getTags(),
        place.getCategoryType()
    );
    return geoCodePlaceDto;
  }









}
