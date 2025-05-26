package jombi.freemates.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.dto.KakaoPlaceDocumentResponse;
import jombi.freemates.model.dto.KakaoPlaceResponse;
import jombi.freemates.model.postgres.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceFetchService {
  private final WebClient kakaoWebClient;

  private static final double SEJONG_X  = 127.0742595815513;
  private static final double SEJONG_Y  = 37.550638892935346;
  private static final int    RADIUS    = 10000;
  private static final int    PAGE_SIZE = 15;
  private static final int    MAX_PAGE  = 45;

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
  public Mono<List<KakaoPlaceDocumentResponse>> fetchPlaces() {

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
        .distinct(KakaoPlaceDocumentResponse::getId)
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
  private Flux<KakaoPlaceDocumentResponse> fetchAllPagesFor(String categoryCode) {
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
                .bodyToMono(KakaoPlaceResponse.class)
                // 실패 시 빈 response 로 대체
                .onErrorResume(Throwable.class, e -> {
                  log.warn("카테고리 {} 페이지 {} 호출 실패: {}", categoryCode, page, e.getMessage());
                  return Mono.just(new KakaoPlaceResponse(null, List.of()));
                })
        )
        // is_end=true 이면 그 페이지까지 수집 후 스트림 종료
        .takeUntil(resp -> resp.getMeta() == null || resp.getMeta().isEnd())
        // KaKaoResponse.documents 를 펼쳐서 Flux<KaKaoDocument> 로 반환
        .flatMapIterable(KakaoPlaceResponse::getDocuments);
  }

  /**
   * KakaoDocument → Place 엔티티로 변환
   */
  public List<Place> buildPlaces(List<KakaoPlaceDocumentResponse> docs) {
    return docs.stream()
        .map(doc -> Place.builder()
            .kakaoPlaceId(doc.getId())
            .addressName(doc.getAddressName())
            .categoryGroupCode(doc.getCategoryGroupCode())
            .phone(doc.getPhone())
            .placeName(doc.getPlaceName())
            .placeUrl(doc.getPlaceUrl())
            .roadAddressName(doc.getRoadAddressName())
            .x(doc.getX())
            .y(doc.getY())
            .distance(doc.getDistance())
            .imageUrl(null)
            .tags(null)
            .introText(null)
            .likeCount(0L)
            .viewCount(0L)
            .categoryType(CategoryType.of(doc.getCategoryGroupCode()))
            .build()
        )
        .collect(Collectors.toList());
  }

}
