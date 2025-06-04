package jombi.freemates.controller;

import jombi.freemates.model.constant.SearchType;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.service.PlaceService;
import jombi.freemates.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
  private final SearchService searchService;

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
