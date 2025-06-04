package jombi.freemates.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jombi.freemates.model.dto.BookmarkRequest;
import jombi.freemates.model.dto.BookmarkResponse;
import jombi.freemates.model.dto.CustomUserDetails;
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
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
  private final BookmarkRepository bookmarkRepository;
  private final FileStorageService storage;
  private final PlaceRepository placeRepository;
  private final BookmarkPlaceRepository bookmarkPlaceRepository;

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
    return BookmarkResponse.builder()
        .bookmarkId(b.getBookmarkId())
        .memberId(member.getMemberId())
        .nickname(member.getNickname())
        .imageUrl(imageUrl)
        .title(b.getTitle())
        .description(b.getDescription())
        .pinColor(b.getPinColor())
        .visibility(b.getVisibility())
        .build();
  }

  @Transactional(Transactional.TxType.SUPPORTS)
  public List<BookmarkResponse> listByMember(CustomUserDetails customUserDetails) {
    Member member = customUserDetails.getMember();
    return bookmarkRepository.findAllByMember(member).stream()
        .map(b -> BookmarkResponse.builder()
            .memberId(member.getMemberId())
            .nickname(member.getNickname())
            .imageUrl(b.getImageUrl())
            .title(b.getTitle())
            .description(b.getDescription())
            .pinColor(b.getPinColor())
            .visibility(b.getVisibility())
            .build())
        .collect(Collectors.toList());
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

    // 중복 검사: 이미 연결된 적이 있는지
    BookmarkPlaceId compositeId = new BookmarkPlaceId(bookmarkId, placeId);
    if (bookmarkPlaceRepository.existsById(compositeId)) {
      throw new CustomException(ErrorCode.DUPLICATE_PLACE_IN_BOOKMARK);
    }

    // 새 BookmarkPlace 엔티티 만들어서 양방향 관계에 추가
    BookmarkPlace bp = BookmarkPlace.builder()
        .bookmarkPlaceId(compositeId)
        .bookmark(bookmark)
        .place(place)
        .build();

    // Bookmark 엔티티 쪽 컬렉션에도 추가 (양쪽 연관관계 유지)
    bookmark.getBookmarkPlaces().add(bp);

    //    명시적으로 BookmarkPlaceRepository를 호출해 주는 편이 더 확실할 수 있습니다.)
    bookmarkPlaceRepository.save(bp);

    log.info("북마크({})에 장소({}) 추가 완료 - 사용자: {}",
        bookmarkId, placeId, member.getNickname());
  }
}
