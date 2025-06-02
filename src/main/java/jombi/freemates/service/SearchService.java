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
  @Transactional(readOnly = true)
  public Page<PlaceDto> searchPlaces(
      SearchType searchType,
      String keyword,
      int page,
      int size
  ) {
    if (searchType == null) {
      searchType = SearchType.defaultType();
    }
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

    return placePage.map(this::toDto);
  }

  private PlaceDto toDto(Place p) {
    // PlaceResponse는 JSON 응답용 DTO
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
