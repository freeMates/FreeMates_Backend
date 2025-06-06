package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.constant.SearchType;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.service.PlaceService;
import jombi.freemates.service.SearchService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "검색 API",
    description = "검색 API 제공"
)
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
  private final SearchService searchService;

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-06",
          author = Author.LEEDAYE,
          issueNumber = 120,
          description = "검색하기"
      )
  })
  @Operation(
      summary = "검색하기",
      description = """
        ## 인증(JWT): **필요**
        
        ## 요청 파라미터 (query parameters)
        - **`type`**: 검색 타입 (ENUM: `NAME`, `INTRO`, `TAG`, Default =`ALL`)
        - **`keyword`**: 검색 키워드(아무것도 검색하지 않을시 장소 전부)
        
        ## 반환값 (`ResponseEntity<Page<PlaceDto>>`)
        - **`content`**: 검색된 장소 목록
        - **`pageable`**: 페이지 정보
        - **`totalElements`**: 전체 요소 수
        - **`totalPages`**: 전체 페이지 수
        - **`size`**: 페이지 크기
        
        ## 에러코드
        
        """

  )
  @GetMapping("/place")
  public ResponseEntity<Page<PlaceDto>> search(
      @RequestParam(value = "type", required = false) SearchType type,
      @RequestParam("keyword") String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Page<PlaceDto> result = searchService.searchPlaces(type, keyword, page, size);
    return ResponseEntity.ok(result);
  }

}
