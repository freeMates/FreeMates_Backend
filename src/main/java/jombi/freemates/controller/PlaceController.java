package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.constant.CategoryType;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.service.PlaceService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "장소 API",
    description = "장소 갱신 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/place")
//@PreAuthorize("hasRole('ROLE_ADMIN')")
//추후 권한 설정
public class PlaceController {
  private final PlaceService placeService;
  @PostMapping("/refresh")
  public ResponseEntity<Void> refreshPlaces() {
    placeService.deleteAllAndRefresh();
    return ResponseEntity.ok().build();
  }

//  @ApiChangeLogs({
//      @ApiChangeLog(
//          date = "2025-05-26",
//          author = Author.LEEDAYE,
//          issueNumber = 96,
//          description = "장소가져오기"
//      )
//  })
//  @Operation(
//      summary = "장소가져오기",
//      description = """
//        ## 인증(JWT): **필요**
//
//        ## 요청 파라미터 (multipart/form-data)
//        - **`page`**: 페이지(0부터 시작, 최대 32)
//        - **`size`**: 크기
//
//        ## 반환값 (`ResponseEntity<Page<Place>>`)
//        - **`content`**: 장소 목록
//        - **`totalElements`**: 전체 요소 수
//        - **`totalPages`**: 전체 페이지 수
//        - **`number`**: 현재 페이지 번호
//        - **`size`**: 페이지 크기
//        - **`sort`**: 정렬 정보
//        - **`numberOfElements`**: 현재 페이지의 요소 수
//        - **`empty`**: 현재 페이지가 비어있는지 여부
//
//        ## 에러코드
//        """
//
//  )
//  @GetMapping("/list")
//  public ResponseEntity<Page<Place>> getPlaces(
//      @RequestParam(defaultValue = "0") int page,
//      @RequestParam(defaultValue = "10") int size
//  ) {
//    // 페이지와 사이즈만으로 Pageable 생성
//    Pageable pageable = PageRequest.of(page, size);
//    return ResponseEntity.ok(placeService.getPlaces(pageable));
//  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-02",
          author = Author.LEEDAYE,
          issueNumber = 104,
          description = "카테고리나 전체 장소 가져오기"
      )
  })
  @Operation(
      summary = "장소가져오기",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터 (multipart/form-data)
        - **`category`**: 카테고리 타입 (예: `RESTAURANT`, `CAFE`, `PARK`, `MUSEUM`, `SHOP`)
        - **`category`**가 없으면 전체 장소를 가져옵니다.
        - **`page`**: 페이지(0부터 시작, 최대 32)
        - **`size`**: 크기

        ## 반환값 (`ResponseEntity<Page<Place>>`)
        - **`content`**: 장소 목록
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
  @GetMapping("/category")
  public ResponseEntity<Page<Place>> getPlacesByCategory(
      @RequestParam(required = false) CategoryType category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);

    return ResponseEntity.ok(placeService.getPlacesByCategory(category, pageable));
  }

}
