package jombi.freemates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import jombi.freemates.model.constant.Author;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.dto.CourseRequest;
import jombi.freemates.model.dto.CourseDto;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.service.CourseService;
import jombi.freemates.util.docs.ApiChangeLog;
import jombi.freemates.util.docs.ApiChangeLogs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    name = "코스 API",
    description = "코스 관련 API 제공"
)
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/course")
public class CourseController {
  private final CourseService courseService;
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-04",
          author = Author.LEEDAYE,
          issueNumber = 112,
          description = "코스 생성"
      )
  })
  @Operation(
      summary = "코스 생성",
      description = """
      ## 인증(JWT): **필요**

      ## 요청 파라미터 (multipart/form-data)
      - `title`: 코스 제목  
      - `description`: 코스 설명  
      - `freeTime`: 코스 예상 소요 시간 (분 단위)  
      - `visibility`: 공개 여부 (ENUM: `PUBLIC` 또는 `PRIVATE`)  
      - `placeIds`: 포함할 장소 ID 목록 (UUID, 콤마 구분 혹은 여러 개 파라미터)  
      - `image`: 코스 대표 이미지 파일 (MultipartFile, 선택 사항)  

      ## 반환값 (`CourseResponse`)
      - `courseId`: 생성된 코스 ID (UUID)  
      - `memberId`: 코스를 만든 사용자 ID (UUID)  
      - `title`, `description`, `freeTime`, `visibility`, `imageUrl`, `placeIds`: 요청한 값 그대로 반환  
      - `createdDate`: 생성일시

      ## 에러 코드
      - `MEMBER_NOT_FOUND` (로그인 정보 누락)  
      - `PLACE_NOT_FOUND` (placeIds 중 하나가 DB에 없을 때)  
      - 기타 내부 서버 에러(500)
      """
  )
  @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<CourseDto> createCourse(
      @RequestParam("title") String title,
      @RequestParam("description") String description,
      @RequestParam("freeTime") Integer freeTime,
      @RequestParam("visibility") Visibility visibility,
      // placeIds를 여러 개 RquestParam으로 받을 수도 있고, 한 문자열(콤마 구분)로 받을 수도 있음
      @RequestParam("placeIds") List<UUID> placeIds,
      @RequestParam(value = "image", required = false) MultipartFile image,
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    // 요청 DTO로 묶어 두기
    CourseRequest req = CourseRequest.builder()
        .title(title)
        .description(description)
        .freeTime(freeTime)
        .visibility(visibility)
        .placeIds(placeIds)
        .build();

    // 서비스 호출 (이미지 파일도 함께 넘긴다)
    CourseDto response = courseService.createCourse(user, req, image);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 내 코스 목록 가져오기
   */
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-04",
          author = Author.LEEDAYE,
          issueNumber = 112,
          description = "내 코스 목록 가져오기"
      )
  })
  @Operation(
      summary = "내 코스 목록 가져오기",
      description = """
      ## 인증(JWT): **필요**

      ## 요청 파라미터 (없음)

      ## 반환값 (`List<CourseDto>`)
      - `courseId`: 코스 ID (UUID)  
      - `memberId`: 코스를 만든 사용자 ID (UUID)  
      - `title`, `description`, `freeTime`, `visibility`, `imageUrl`, `placeIds`: 코스 정보  
      - `createdDate`: 생성일시

      ## 에러 코드
      - `MEMBER_NOT_FOUND` (로그인 정보 누락)  
      - 기타 내부 서버 에러(500)
      """
  )
  @GetMapping("/mylist")
  public ResponseEntity<List<CourseDto>> getMyCourses(
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    // 서비스 호출
    List<CourseDto> courses = courseService.getMyCourses(user);

    return ResponseEntity.ok(courses);
  }



  /**
   * public인 코스 목록 가져오기
   */
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-04",
          author = Author.LEEDAYE,
          issueNumber = 112,
          description = "공개 코스 목록 가져오기"
      )
  })
  @Operation(
      summary = "공개 코스 목록 가져오기",
      description = """
      ## 인증(JWT): **필요**

      ## 요청 파라미터
      - `visibility`: 코스 공개 여부 (ENUM: `PUBLIC` 또는 `PRIVATE`, 기본값: `PUBLIC`)
      - `page`: 페이지 번호 (0부터 시작, 기본값: 0)
      - `size`: 페이지 크기 (기본값: 10)
     

      ## 반환값 (`Page<CourseDto>`)
      - `courseId`: 코스 ID (UUID)  
      - `nickName`: 코스를 만든 사용자 닉네임  
      - `title`, `description`, `freeTime`, `visibility`, `imageUrl`, `placeIds`: 코스 정보  

      ## 에러 코드
      - 기타 내부 서버 에러(500)
      """
  )
  @GetMapping("/list")
  public ResponseEntity<Page<CourseDto>> getCourses(
      @RequestParam(defaultValue = "PUBLIC") Visibility visibility,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<CourseDto> pagedCourses = courseService.getCourses(visibility, pageable);
    return ResponseEntity.ok(pagedCourses);
  }

  /**
   * 코스 좋아요
   */
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-06",
          author = Author.LEEDAYE,
          issueNumber = 105,
          description = "코스 좋아요 추가/취소"
      )
  })
  @Operation(
      summary = "코스 좋아요",
      description = """
      ## 인증(JWT): **필요**
      
      ## 요청 파라미터
      - **Path Variable**
        - `courseId` (UUID): 좋아요를 누를 코스 ID

      ## 반환값
      - **HTTP Status 200 OK** (혹은 204 No Content)

      ## 에러코드
      - `UNAUTHORIZED (401)`: 인증되지 않은 사용자입니다.
      - `COURSE_NOT_FOUND (404)`: 존재하지 않는 코스입니다.
      """
  )
  @PostMapping("/like/{courseId}")
  public ResponseEntity<Void> likeCourse(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable("courseId") UUID courseId
  ) {
    courseService.likeCourse(customUserDetails, courseId);
    return ResponseEntity.ok().build();
  }









}
