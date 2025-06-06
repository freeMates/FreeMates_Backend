package jombi.freemates.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.dto.BookmarkRequest;
import jombi.freemates.model.dto.BookmarkResponse;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.model.postgres.Bookmark;
import jombi.freemates.model.postgres.BookmarkPlace;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.model.postgres.id.BookmarkPlaceId;
import jombi.freemates.repository.BookmarkPlaceRepository;
import jombi.freemates.repository.BookmarkRepository;
import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
  private final BookmarkRepository bookmarkRepository;
  private final FileStorageService storage;
  private final PlaceRepository placeRepository;
  private final BookmarkPlaceRepository bookmarkPlaceRepository;
  private final PlaceService placeService;

  /**
   * 즐겨찾기 생성
   *
   */
  @Transactional
  public BookmarkResponse create(
      CustomUserDetails customUserDetails,
      BookmarkRequest req,
      MultipartFile image
  ) {

    // 파일이 있으면 저장 후 imageUrl 세팅, 없으면 imageUrl = null
    String imageUrl = null;
    if (image != null && !image.isEmpty()) {
      imageUrl = storage.storeImage(image);
    }

    // Member 조회
    Member member = customUserDetails.getMember();

    // 엔티티 생성 시 imageUrl을 null 또는 저장된 값으로 넣는다
    Bookmark b = Bookmark.builder()
        .member(member)
        .title(req.getTitle())
        .description(req.getDescription())
        .pinColor(req.getPinColor())
        .visibility(req.getVisibility())
        .imageUrl(imageUrl)   // 파일이 없으면 null, 있으면 저장된 경로
        .build();

    // DB 저장
    bookmarkRepository.save(b);
    log.info("북마크 생성 완료 - ID: {}, 사용자: {}", b.getBookmarkId(), member.getNickname());

    // 응답 DTO 반환
    return convertToBookmarkResponse(b);
  }

  /**
   * 멤버 별 즐겨찾기 목록 조회
   */
  @Transactional(readOnly = true)
  public List<BookmarkResponse> getMyBookmarks(CustomUserDetails customUserDetails) {
    Member member = customUserDetails.getMember();
    return bookmarkRepository.findAllByMember(member).stream()
        .map(b -> convertToBookmarkResponse(b))
        .collect(Collectors.toList());
  }

  /**
   * 즐겨찾기 목록 조회 (페이징)
   */
  @Transactional(readOnly = true)
  public Page<BookmarkResponse> getBookmarks(int page, int size, Visibility visibility) {
    return bookmarkRepository
        .findByVisibility(visibility, PageRequest.of(page, size))
        .map(b -> convertToBookmarkResponse(b));
  }

  @Transactional
  public void addPlaceToBookmark(
      CustomUserDetails customUserDetails,
      UUID bookmarkId,
      UUID placeId
  ) {
    Member member = customUserDetails.getMember();

    // 즐겨찾기(Bookmark) 존재 여부 + 소유자 검사
    Bookmark bookmark = bookmarkRepository
        .findByBookmarkIdAndMember(bookmarkId, member)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

    // 장소(Place) 존재 여부 확인
    Place place = placeRepository
        .findByPlaceId(placeId)
        .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

    // “현재 유저가 어떤 폴더(Bookmark)에서든 이 place를 이미 북마크했는지” 확인
    boolean alreadyBookmarkedByUser =
        bookmarkPlaceRepository.existsByBookmarkMemberMemberIdAndPlacePlaceId(
            member.getMemberId(), placeId);
    log.info("유저 {}가 장소 {}를 이미 북마크했는지: {}",
        member.getNickname(), placeId, alreadyBookmarkedByUser);

    // “현재 폴더(bookmarkId)에 이미 들어 있는지” 확인 (중복 폴더 방지)
    BookmarkPlaceId compositeId = new BookmarkPlaceId(bookmarkId, placeId);
    if (bookmarkPlaceRepository.existsById(compositeId)) {
      throw new CustomException(ErrorCode.DUPLICATE_PLACE_IN_BOOKMARK);
    }

    // 만약 유저가 어느 폴더에도 이 place를 담은 적이 없다면, 최초 1회만 likeCount 증가
    if (!alreadyBookmarkedByUser) {
      place.setLikeCount(place.getLikeCount() + 1);
      placeRepository.save(place);
      log.info("장소 {}의 좋아요 수 증가 - 현재 좋아요 수: {}",
          placeId, place.getLikeCount());
    }


    // 새로운 BookmarkPlace 엔티티 생성 (현재 폴더에만 추가)
    BookmarkPlace bp = BookmarkPlace.builder()
        .bookmarkPlaceId(compositeId)
        .bookmark(bookmark)
        .place(place)
        .build();

    // 양방향 관계 유지 (필요할 경우)
    bookmark.getBookmarkPlaces().add(bp);

    bookmarkPlaceRepository.save(bp);

  }

  @Transactional(readOnly = true)
  public List<PlaceDto> getPlacesByBookmarkId(UUID bookmarkId) {
    // bookmarkId로 연결된 BookmarkPlace 목록 조회
    List<BookmarkPlace> bookmarkPlaces =
        bookmarkPlaceRepository.findByBookmarkBookmarkId(bookmarkId);

    // 각 BookmarkPlace에서 Place를 꺼내어 PlaceDto로 변환
    return bookmarkPlaces.stream()
        .map(bp -> placeService.convertToPlaceDto(bp.getPlace()))
        .collect(Collectors.toList());
  }

  public BookmarkResponse convertToBookmarkResponse(Bookmark bookmark) {
    return BookmarkResponse.builder()
        .bookmarkId(bookmark.getBookmarkId())
        .memberId(bookmark.getMember().getMemberId())
        .nickname(bookmark.getMember().getNickname())
        .imageUrl(bookmark.getImageUrl())
        .title(bookmark.getTitle())
        .description(bookmark.getDescription())
        .pinColor(bookmark.getPinColor())
        .visibility(bookmark.getVisibility())
        .build();
  }

}
