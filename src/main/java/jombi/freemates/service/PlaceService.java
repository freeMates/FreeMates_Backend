package jombi.freemates.service;

import java.util.List;
import java.util.stream.Collectors;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.dto.KakaoDocument;
import jombi.freemates.model.dto.KakaoResponse;

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


  /**
   * 세종대학교 반경 10km 내 지정 카테고리 장소를 비동기로 전 페이지 조회
   */
  public Mono<List<KakaoDocument>> fetchPlaces() {
    List<CategoryType> categories = List.of(
        CategoryType.CAFE,
        CategoryType.FOOD,
        CategoryType.SHOPPING,
        CategoryType.WALK,
        CategoryType.PLAY,
        CategoryType.HOSPITAL
    );

    return Flux.fromIterable(categories)
        // 각 카테고리 타입별로
        .flatMap(cat ->
            // 그룹 코드들 하나씩
            Flux.fromIterable(cat.getKakaoCodes())
                // 각 코드마다 모든 페이징을 fetchAllPagesFor 으로 처리
                .flatMap(this::fetchAllPagesFor)
        )
        .collectList();
  }

  /**
   * 단일 카테고리 그룹 코드에 대해 page=1 부터 is_end 까지 순차 호출
   */
  private Flux<KakaoDocument> fetchAllPagesFor(String categoryCode) {
    // 1페이지부터 시작
    return Mono.just(1)
        // expand 로 “다음 페이지 번호”를 스트림으로 확장
        .expand(currentPage -> {
          if (currentPage < MAX_PAGE) {
            return Mono.just(currentPage + 1);
          }
          return Mono.empty();
        })
        // 각 page 번호마다 API 호출
        .flatMap(page ->
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
        .takeUntil(resp -> resp.getMeta() != null && resp.getMeta().isEnd()==true)
        // KaKaoResponse.documents 를 펼쳐서 Flux<KaKaoDocument> 로 반환
        .flatMapIterable(KakaoResponse::getDocuments);
  }

  /**
   *
   * 실제 카카오 API → DB 동기화
   * 비동기처리
   * */

  @Async("applicationTaskExecutor")
  public void doRefresh() {
    List<KakaoDocument> docs = fetchPlaces()
        .block(java.time.Duration.ofMinutes(2));

    List<Place> places = docs.stream()
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
            .build())
        .collect(Collectors.toList());

    placeRepository.saveAll(places);
  }

  public void deleteAllAndRefresh() {
    placeRepository.deleteAll();
    doRefresh();
  }

}
