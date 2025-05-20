package jombi.freemates.service;


import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import jombi.freemates.model.dto.KakaoCrawlDetail;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.service.crawler.KakaoCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.dto.KakaoDocument;
import jombi.freemates.model.dto.KakaoResponse;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
  private static final double SEJONG_X    = 127.0742595815513;
  private static final double SEJONG_Y    = 37.550638892935346;
  private static final int    RADIUS      = 10000;
  private static final int    PAGE_SIZE   = 15;   // 카카오 API 한 페이지 최대 15
  private static final int    MAX_PAGE    = 45;// 카카오 API 최대 페이지 45

  private final PlaceRepository placeRepository;
  private final WebClient kakaoWebClient ;
  private final KakaoCrawler kakaoCrawler;
  private static final List<CategoryType> CATEGORIES = List.of(
         CategoryType.CAFE,
         CategoryType.FOOD,
         CategoryType.SHOPPING,
         CategoryType.WALK,
         CategoryType.PLAY,
         CategoryType.HOSPITAL
         );


  /**
   * 세종대학교 반경 10km 내 지정 카테고리 장소를 비동기로 전 페이지 조회
   */
  public Mono<List<KakaoDocument>> fetchPlaces() {

    return Flux.fromIterable(CATEGORIES)
        // 카테고리 코드로 변환
        .concatMap(categoryType ->
            // 그 카테고리가 가진 모든 kakaoCodes 에 대해
            Flux.fromIterable(categoryType.getKakaoCodes())
                // 코드 하나당 전체 페이지를 조회
                .concatMap(this::fetchAllPagesFor)
        )
        // 카테고리 그룹 코드로 필터링
        .filter(doc ->
            CATEGORIES.stream()
                .anyMatch(cat -> cat.getKakaoCodes()
                    .contains(doc.getCategoryGroupCode()))
        )        // 중복 제거
        .distinct(KakaoDocument::getId)
        // 리스트로 수집
        .collectList()
        // 에러 처리
        .doOnError(WebClientResponseException.class,
            e -> log.error("카카오 API 호출 실패: {}", e.getMessage()))
        .doOnError(Throwable.class,
            e -> log.error("카카오 API 호출 중 오류 발생: {}", e.getMessage()));
  }

  /**
   * 단일 카테고리 그룹 코드에 대해 page=1 부터 is_end 까지 순차 호출
   */
  private Flux<KakaoDocument> fetchAllPagesFor(String categoryCode) {
    // 1페이지부터 시작
    return Mono.just(1)
        // expand 로 “다음 페이지 번호”를 스트림으로 확장
        .expand(page -> page < MAX_PAGE ? Mono.just(page + 1) : Mono.empty())
        // 각 page 번호마다 API 호출
        .concatMap(page ->
            kakaoWebClient.get()
                .uri(b -> b.path("/v2/local/search/category.json")
                    .queryParam("category_group_code", categoryCode)
                    .queryParam("x", SEJONG_X)
                    .queryParam("y", SEJONG_Y)
                    .queryParam("radius", RADIUS)
                    .queryParam("size", PAGE_SIZE)
                    .queryParam("page", page)
                    .build()
                )
                .retrieve()
                .bodyToMono(KakaoResponse.class)
                // 실패 시 빈 response 로 대체
                .onErrorResume(Throwable.class, e -> {
                  log.warn("카테고리 {} 페이지 {} 호출 실패: {}", categoryCode, page, e.getMessage());
                  return Mono.just(new KakaoResponse(null, List.of()));
                })
        )
        // is_end=true 이면 그 페이지까지 수집 후 스트림 종료
        .takeUntil(resp -> resp.getMeta() != null && resp.getMeta().isEnd())
        // KaKaoResponse.documents 를 펼쳐서 Flux<KaKaoDocument> 로 반환
        .flatMapIterable(KakaoResponse::getDocuments);
  }

  /**
   * KakaoDocument → Place 엔티티로 변환
   */
  private List<Place> buildPlaces(List<KakaoDocument> docs) {
    return docs.stream()
        .map(doc -> Place.builder()
            .id(doc.getId())
            .addressName(doc.getAddressName())
            .categoryGroupCode(doc.getCategoryGroupCode())
            .phone(doc.getPhone())
            .placeName(doc.getPlaceName())
            .placeUrl(doc.getPlaceUrl())
            .roadAddressName(doc.getRoadAddressName())
            .x(doc.getX())
            .y(doc.getY())
            .distance(doc.getDistance())
            .imgUrl(null)
            .description(null)
            .amenities(null)
            .likeCnt(0L)
            .viewCnt(0L)
            .categoryType(CategoryType.of(doc.getCategoryGroupCode()))
            .build()
        )
        .collect(Collectors.toList());
  }

  /**
   * 실제 DB 동기화 삭제 여부만 갈라서, fetch→저장은 한 번에!
   */
  @Transactional
  public void refreshPlaces(boolean deleteFirst) {
    if (deleteFirst) {
      placeRepository.deleteAll();
      log.info("기존 장소 전부 삭제");
    }

    List<KakaoDocument> docs = fetchPlaces()
        .timeout(Duration.ofMinutes(2))
        .block();  // 블로킹은 한 번만!

    List<Place> places = buildPlaces(docs);
    log.info("총 {}개 장소 저장 시작", places.size());
    try {
      placeRepository.saveAll(places);
      log.info("장소 데이터 카카오 {}개 저장 완료", places.size());
    } catch (Exception e) {
      log.error("장소 데이터 저장 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
    if (deleteFirst) {
      addKakaoCrawlInfo();
    }else{
      addKakaoCrawlInfoAsync();

    }

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

        KakaoCrawlDetail kakaoCrawlDetail = kakaoCrawler.crawlByPlaceId(place.getId());
        // Place 엔티티에 정보 업데이트
        place.setImgUrl(kakaoCrawlDetail.getImgUrl());
        place.setDescription(kakaoCrawlDetail.getDescription());
        place.setAmenities(kakaoCrawlDetail.getAmenities());
        placeRepository.save(place);
        log.debug("이미지 저장된 거 {} 네이버에서 크롤링한거{}", place.getImgUrl(), kakaoCrawlDetail.getImgUrl());
      } catch (Exception e) {
        log.error("카카오 크롤링 중 오류 발생: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * 네이버 크롤링 비동기처리
   */
  public void addKakaoCrawlInfoAsync() {
    List<Place> places = placeRepository.findAll();
    int concurrency = 10;
    Duration timeout = Duration.ofSeconds(5);
    int maxRetries = 2;

    Flux.fromIterable(places)
        .flatMap(place ->
                Mono.fromCallable(() -> kakaoCrawler.crawlByPlaceId(place.getId()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1)))
                    // KakaoCrawlDetail → Place 로 변환
                    .map(detail -> {
                      place.setImgUrl(detail.getImgUrl());
                      place.setDescription(detail.getDescription());
                      place.setAmenities(detail.getAmenities());
                      return place;
                    })
                    .onErrorResume(e -> {
                      log.warn("크롤링 실패 [{}]: {}", place.getPlaceName(), e.getMessage());
                      // 실패한 경우에도 원래 place 를 반환
                      return Mono.just(place);
                    })
            , concurrency
        )
        .collectList()
        .doOnNext(updatedPlaces -> placeRepository.saveAll(updatedPlaces))
        .block();
  }




}
