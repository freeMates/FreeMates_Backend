package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.dto.BookmarkRequest;
import jombi.freemates.model.dto.BookmarkResponse;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.service.BookmarkService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "즐겨찾기 API",
    description = "즐겨찾기 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
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
  @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public BookmarkResponse create(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute @Valid BookmarkRequest request
  ) {
    return bookmarkService.create(customUserDetails, request);
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
      summary = "즐겨찾기 생성",
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
  @GetMapping("/list")
  public List<BookmarkResponse> list(
      @AuthenticationPrincipal CustomUserDetails customUserDetails
  ) {
    return bookmarkService.listByMember(customUserDetails);
  }

}
