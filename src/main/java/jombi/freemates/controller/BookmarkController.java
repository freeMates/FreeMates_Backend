package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.constant.PinColor;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.dto.BookmarkRequest;
import jombi.freemates.model.dto.BookmarkDto;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.service.BookmarkService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(
    name = "즐겨찾기 API",
    description = "즐겨찾기 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/bookmark")
public class BookmarkController {
  private final BookmarkService bookmarkService;

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-05-26",
          author = Author.LEEDAYE,
          issueNumber = 96,
          description = "즐겨찾기 만들기"
      )
  })
  @Operation(
      summary = "즐겨찾기 생성",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터 (multipart/form-data)
        - **`title`**: 즐겨찾기 제목  
        - **`description`**: 즐겨찾기 설명  
        - **`pinColor`**: 핀 색깔 (ENUM, 6가지 중 하나)  
        - **`visibility`**: 공개 여부 (ENUM: `PUBLIC` 또는 `PRIVATE`)  
        - **`file`**: 이미지 파일 (MultipartFile)  
        
        ## 반환값 (`BookmarkResponse`)
        - **`memberId`**: 즐겨찾기를 생성한 회원 ID (UUID)  
        - **`nickname`**: 닉네임
        - **`imageUrl`**: 저장된 이미지 URL  
        - **`title`**, **`description`**, **`pinColor`**, **`visibility`**: 요청값 그대로 반환  

        ## 에러코드
        """

  )

  @PostMapping(value = "/create",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<BookmarkDto> create(
      @RequestParam String title,
      @RequestParam String description,
      @RequestParam PinColor pinColor,
      @RequestParam Visibility visibility,
      @RequestParam(value = "image", required = false)
      @Parameter(description = "이미지 파일", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
      MultipartFile image,
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    BookmarkRequest req = BookmarkRequest.builder()
        .title(title)
        .description(description)
        .pinColor(pinColor)
        .visibility(visibility)
        .build();
    BookmarkDto dto = bookmarkService.create(user, req, image);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-05-26",
          author = Author.LEEDAYE,
          issueNumber = 96,
          description = "즐겨찾기 유저별로 가져오기"
      )
  })
  @Operation(
      summary = "내 즐겨찾기 가져오기",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터
        - **`없음`**
         
        ## 반환값 (`BookmarkResponse`)
        - **`memberId`**: 즐겨찾기를 생성한 회원 ID (UUID)  
        - **`nickname`**: 닉네임
        - **`imageUrl`**: 저장된 이미지 URL  
        - **`title`**, **`description`**, **`pinColor`**, **`visibility`**: 요청값 그대로 반환  

        ## 에러코드
        """
  )
  @GetMapping("/mylist")
  public List<BookmarkDto> getMyBookmarks(
      @AuthenticationPrincipal CustomUserDetails customUserDetails
  ) {
    return bookmarkService.getMyBookmarks(customUserDetails);
  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-02",
          author = Author.LEEDAYE,
          issueNumber = 105,
          description = "public/private 즐겨찾기 목록 가져오기"
      )
  })
  @Operation(
      summary = "즐겨찾기 목록 가져오기",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터
        - **`page`**: 페이지 번호 (0부터 시작, 기본값: 0)
        - **`size`**: 페이지 크기 (기본값: 10)
        - **`visibility`**: 공개 여부 (ENUM: `PUBLIC`, `PRIVATE`, 선택적)

        ## 반환값 (`Page<BookmarkResponse>`)
        - **`content`**: 즐겨찾기 목록
        - **`totalElements`**: 전체 요소 수
        - **`totalPages`**: 전체 페이지 수
        - **`number`**: 현재 페이지 번호
        - **`size`**: 페이지 크기
        - **`sort`**: 정렬 정보
        - **`numberOfElements`**: 현재 페이지의 요소 수
        - **`empty`**: 현재 페이지가 비어있는지 여부

        ## 에러코드
        """
  )
  @GetMapping("/list")
  public ResponseEntity<Page<BookmarkDto>> getBookmarks(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "PUBLIC") Visibility visibility
  ) {
    log.debug("page:{}, size:{}, visibility:{}", page, size, visibility);
    Page<BookmarkDto> bookmarks = bookmarkService.getBookmarks(page, size, visibility);
    return ResponseEntity.ok(bookmarks);
  }



  /**
   * 장소를 즐겨찾기에 추가하는 API입니다.
   * */
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-05-26",
          author = Author.LEEDAYE,
          issueNumber = 96,
          description = "장소 즐겨찾기에 추가하기"
      )
  })
  @Operation(
      summary = "장소 즐겨찾기에 추가하기",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터
        - **Path Variable**
          - `bookmarkId` (UUID): 장소를 추가할 즐겨찾기 ID
        **Request Parameter**
          - `placeId` (UUID): 추가할 장소 ID

        ## 반환값
        - **HTTP Status 200 OK** (혹은 204 No Content)

        ## 에러코드
        - `UNAUTHORIZED (401)`: 인증되지 않은 사용자입니다.
        - `BOOKMARK_NOT_FOUND (404)`: 존재하지 않는 즐겨찾기입니다.
        - `PLACE_NOT_FOUND (404)`: 존재하지 않는 장소입니다.
        - `DUPLICATE_PLACE_IN_BOOKMARK (409)`: 이미 즐겨찾기에 추가된 장소입니다.
        """
  )
  @PostMapping( "/add/place/{bookmarkId}")
  public ResponseEntity<Void> addPlaceToBookmark(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable("bookmarkId") UUID bookmarkId,
      @RequestParam UUID placeId
  ) {
    log.debug("placeId:{}", placeId);
    bookmarkService.addPlaceToBookmark(customUserDetails, bookmarkId, placeId);
    return ResponseEntity.ok().build();  // 혹은 204 No Content
  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-06",
          author = Author.LEEDAYE,
          issueNumber = 105,
          description = "좋아요~"
      )
  })
  @Operation(
      summary = "북마크 좋아요",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터
        - **Path Variable**
          - `bookmarkId` (UUID): 좋아요를 누를 즐겨찾기 ID

        ## 반환값
        - **HTTP Status 200 OK** (혹은 204 No Content)

        ## 에러코드
        - `UNAUTHORIZED (401)`: 인증되지 않은 사용자입니다.
        - 'BOOKMARK_NOT_FOUND (404)': 존재하지 않는 북마크입니다.
        """
  )
  @PostMapping("/like/{bookmarkId}")
  public ResponseEntity<Void> likeBookmark(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable("bookmarkId") UUID bookmarkId
  ) {
    bookmarkService.likeBookmark(customUserDetails, bookmarkId);
    return ResponseEntity.ok().build();  // 혹은 204 No Content
  }

}
