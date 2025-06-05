package jombi.freemates.service;


import jombi.freemates.model.constant.SearchType;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
  private final PlaceRepository placeRepository;
  private final PlaceService placeService;

  @Transactional(readOnly = true)
  public Page<PlaceDto> searchPlaces(
      SearchType searchType,
      String keyword,
      int page,
      int size
  ) {
    // 검색어가 비어 있으면, 이미 DTO로 변환된 Page<PlaceDto>를 그대로 리턴
    if (keyword == null || keyword.isBlank()) {
      log.info("검색어가 비어있음, 전체 장소 조회");
      Pageable pageable = PageRequest.of(page, size);
      return placeService.getPlacesByCategory(null, pageable);
    }

    // 검색 타입이 null이면 기본값 설정
    if (searchType == null) {
      searchType = SearchType.defaultType();
    }

    // 검색 시 페이징 + 정렬 설정
    Pageable pageable = PageRequest.of(page, size, Sort.by("placeName").ascending());
    Page<Place> placePage;

    switch (searchType) {
      case NAME:
        placePage = placeRepository.searchByName(keyword, pageable);
        break;
      case INTRO:
        placePage = placeRepository.searchByIntro(keyword, pageable);
        break;
      case TAG:
        placePage = placeRepository.searchByTag(keyword, pageable);
        break;
      case ALL:
      default:
        placePage = placeRepository.searchAllFields(keyword, pageable);
        break;
    }

    // Place → PlaceDto로 변환
    return placePage.map(this::toDto);
  }

  private PlaceDto toDto(Place p) {
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
        .build();
  }
}

