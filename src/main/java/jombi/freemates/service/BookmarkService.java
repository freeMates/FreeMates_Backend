package jombi.freemates.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.dto.BookmarkRequest;
import jombi.freemates.model.dto.BookmarkResponse;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.postgres.Bookmark;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.repository.BookmarkRepository;
import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    return BookmarkResponse.builder()
        .memberId(member.getMemberId())
        .nickname(member.getNickname())
        .imageUrl(imageUrl)
        .title(b.getTitle())
        .description(b.getDescription())
        .pinColor(b.getPinColor())
        .visibility(b.getVisibility())
        .build();
  }

  /**
   * 멤버 별 즐겨찾기 목록 조회
   */
  @Transactional(Transactional.TxType.SUPPORTS)
  public List<BookmarkResponse> getBookmarksByMember(CustomUserDetails customUserDetails) {
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

  /**
   * 즐겨찾기 목록 조회 (페이징)
   */
  @Transactional(Transactional.TxType.SUPPORTS)
  public Page<BookmarkResponse> getBookmarks(int page, int size, Visibility visibility) {
    return bookmarkRepository
        .findByVisibility(visibility, PageRequest.of(page, size))
        .map(b -> BookmarkResponse.builder()
            .bookmarkId(b.getBookmarkId())
            .memberId(b.getMember().getMemberId())
            .nickname(b.getMember().getNickname())
            .imageUrl(b.getImageUrl())
            .title(b.getTitle())
            .description(b.getDescription())
            .pinColor(b.getPinColor())
            .visibility(b.getVisibility())
            .build()
        );
  }

  @Transactional
  public void addPlaceToBookmark(CustomUserDetails customUserDetails,
      UUID bookmarkId,
      UUID placeId) {
    Member member = customUserDetails.getMember();

    // 즐겨찾기 존재 여부 + 본인 소유 여부 확인
    Bookmark bookmark = bookmarkRepository
        .findByBookmarkIdAndMember(bookmarkId, member)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

    // 장소(Place) 존재 여부 확인
    Place place = placeRepository
        .findByPlaceId(placeId)
        .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

    // 이미 추가된 상태인지는 선택사항: 중복 방지를 원하면 체크
    if (bookmark.getPlaces().contains(place)) {
      throw new CustomException(ErrorCode.DUPLICATE_PLACE_IN_BOOKMARK);
    }

    // 리스트에 추가
    bookmark.getPlaces().add(place);

    // 저장 (cascade 옵션이 없으면, 명시적으로 save 해 줘야 함)
    bookmarkRepository.save(bookmark);

    log.info("북마크({})에 장소({}) 추가 완료 - 사용자: {}",
        bookmarkId, placeId, member.getNickname());
  }
}
