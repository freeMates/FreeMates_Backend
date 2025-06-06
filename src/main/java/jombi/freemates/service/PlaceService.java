package jombi.freemates.service;


import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.dto.KakaoPlaceCrawlDetail;
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
    // category가 null이면 전체 조회, 아니면 카테고리별 조회
    Page<Place> placePage = (category == null)
        ? placeRepository.findAll(pageable)
        : placeRepository.findByCategoryType(category, pageable);

    // Place → PlaceDto 변환
    return placePage.map(this::convertToPlaceDto);
  }

  /**
   * 좌표에 따른 장소 조회
   */
  @Transactional(readOnly = true)
  public List<PlaceDto> getPlacesByGeocode(String xStr, String yStr) {
    double xInput;
    double yInput;
    try {
      xInput = Double.parseDouble(xStr);
      yInput = Double.parseDouble(yStr);
    } catch (NumberFormatException e) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    // 소수점 셋째 자리에서 반올림한 기준값 구하기
    BigDecimal bdX = BigDecimal.valueOf(xInput).setScale(3, RoundingMode.HALF_EVEN);
    BigDecimal bdY = BigDecimal.valueOf(yInput).setScale(3, RoundingMode.HALF_EVEN);

    // DB에서 일단 모든 Place를 조회해 온 뒤(규모가 크지 않다면 충분히 괜찮음),
    // 또는 범위 쿼리를 쓰려면 PlaceRepository에 추가 커스텀 메서드를 만들어도 됨.
    List<Place> allPlaces = placeRepository.findAll();

    // 필터링: 각 Place 엔티티가 가지고 있는 x, y를 double로 파싱 → 동일하게 소수점 4자리 반올림 → 비교
    List<Place> matched = allPlaces.stream()
        .filter(p -> {
          String px = p.getX();
          String py = p.getY();
          if (px == null || py == null || px.isBlank() || py.isBlank()) {
            return false;
          }
          double xPlace, yPlace;
          try {
            xPlace = Double.parseDouble(px);
            yPlace = Double.parseDouble(py);
          } catch (NumberFormatException ex) {
            return false;
          }

          BigDecimal bdXPlace = BigDecimal.valueOf(xPlace).setScale(3, RoundingMode.HALF_EVEN);
          BigDecimal bdYPlace = BigDecimal.valueOf(yPlace).setScale(3, RoundingMode.HALF_EVEN);

          return bdXPlace.equals(bdX) && bdYPlace.equals(bdY);
        })
        .toList();

    if (matched.isEmpty()) {
      throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
    }

    // 최종 PlaceDto 리스트로 매핑하여 반환
    return matched.stream()
        .map(this::convertToPlaceDto)
        .collect(toList());
  }

  /**
   * Place → PlaceDto 변환 로직을 한곳에 모아 둔 메서드
   */
  public PlaceDto convertToPlaceDto(Place p) {
    return PlaceDto.builder()
        .placeId(p.getPlaceId())
        .placeName(p.getPlaceName())
        .introText(p.getIntroText())
        .addressName(p.getAddressName())
        .imageUrl(p.getImageUrl())
        .tags(p.getTags())
        .categoryType(p.getCategoryType())
        .likeCount(p.getLikeCount())
        .viewCount(p.getViewCount())
        .distance(p.getDistance())
        .x(p.getX())
        .y(p.getY())
        .build();
  }
  }










