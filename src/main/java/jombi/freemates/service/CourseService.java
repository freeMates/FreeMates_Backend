package jombi.freemates.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.dto.CourseRequest;
import jombi.freemates.model.dto.CourseDto;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.dto.PlaceDto;
import jombi.freemates.model.postgres.Course;
import jombi.freemates.model.postgres.CourseLike;
import jombi.freemates.model.postgres.CoursePlace;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.model.postgres.id.CourseLikeId;
import jombi.freemates.model.postgres.id.CoursePlaceId;
import jombi.freemates.repository.CourseLikeRepository;
import jombi.freemates.repository.CoursePlaceRepository;
import jombi.freemates.repository.CourseRepository;
import jombi.freemates.repository.PlaceRepository;
import jombi.freemates.util.exception.CustomException;
import jombi.freemates.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final FileStorageService storage;
  private final PlaceRepository placeRepository;
  private final CoursePlaceRepository coursePlaceRepository;
  private final PlaceService placeService;
  private final CourseLikeRepository courseLikeRepository;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * 코스 생성
   */
  @Transactional
  public CourseDto createCourse(
      CustomUserDetails customUser,
      CourseRequest req,
      MultipartFile image
  ) {
    // 회원 확인
    Member member = customUser.getMember();
    if (member == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    // 이미지 저장
    String imageUrl = null;
    if (image != null && !image.isEmpty()) {
      imageUrl = storage.storeImage(image);
    }// placeIds 각각으로 Place 조회 → CoursePlace 생성


    // Course 엔티티 생성·저장
    Course course = courseRepository.save(
        Course.builder()
            .member(member)
            .title(req.getTitle())
            .description(req.getDescription())
            .freeTime(req.getFreeTime())
            .visibility(req.getVisibility())
            .imageUrl(imageUrl)
            .likeCount(0L) // 초기 좋아요 수는 0
            .build()
    );

    List<UUID> placeIds = req.getPlaceIds();
    List<CoursePlace> coursePlaceList = IntStream.range(0, placeIds.size())
        .mapToObj(idx -> {
          UUID placeId = placeIds.get(idx);
          Place place = placeRepository.findByPlaceId(placeId)
              .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
          CoursePlaceId compositeId = new CoursePlaceId(course.getCourseId(), place.getPlaceId());
          return CoursePlace.builder()
              .coursePlaceId(compositeId)
              .course(course)
              .place(place)
              .sequence(idx + 1)
              .build();
        })
        .collect(Collectors.toList());

    // CoursePlace 한꺼번에 저장
    coursePlaceRepository.saveAll(coursePlaceList);

    entityManager.flush();
    entityManager.clear();

    // 바로 DTO 변환
    return courseRepository.findById(course.getCourseId())
        .map(this::converToCourseDto)
        .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));// 단일 코스 생성이므로 첫 번째 요소만 반환
  }

  /**
   * 내 코스 목록 가져오기
   */
  @Transactional(readOnly = true)
  public List<CourseDto> getMyCourses(CustomUserDetails customUser) {
    Member member = customUser.getMember();
    if (member == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    List<Course> courses = courseRepository.findAllByMember(member);
    return courses.stream()
        .map(this::converToCourseDto)
        .collect(Collectors.toList());
  }

  /**
   * public인 코스 목록 가져오기
   */
  @Transactional(readOnly = true)
  public Page<CourseDto> getCourses(Visibility visibility, Pageable pageable) {
    Page<Course> coursePage = courseRepository.findAllByVisibility(visibility, pageable);

    // Page<Course> → Page<CourseDto>로 변환
    return coursePage.map(this::converToCourseDto);
  }


  /**
   * 코스 좋아요
   */
  @Transactional
  public void likeCourse(CustomUserDetails customUser, UUID courseId) {
    Member member = customUser.getMember();
    if (member == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

    CourseLikeId likeId = new CourseLikeId(member.getMemberId(), course.getCourseId());
    boolean exists = courseLikeRepository.existsById(likeId);

    if (exists) {
      // 이미 좋아요된 상태 → 취소
      courseLikeRepository.deleteById(likeId);
      long current = course.getLikeCount() == null ? 0L : course.getLikeCount();
      course.setLikeCount(Math.max(0, current - 1));
      courseRepository.save(course);
    } else {
      // 좋아요가 없는 상태 → 추가
      CourseLike like = CourseLike.builder()
          .id(likeId)
          .member(member)
          .course(course)
          .build();
      courseLikeRepository.save(like);
      long current = course.getLikeCount() == null ? 0L : course.getLikeCount();
      course.setLikeCount(current + 1);
      courseRepository.save(course);
    }
  }
  @Transactional
  public void updateCourse(
      CustomUserDetails customUser,
      UUID courseId,
      CourseRequest req,
      MultipartFile image

  ) {
    // 회원 확인
    Member member = customUser.getMember();
    if (member == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    // 코스 조회
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));
    UUID ownerId = course.getMember().getMemberId();
    UUID currentUserId = member.getMemberId();
    if (!ownerId.equals(currentUserId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 이미지 저장
    String imageUrl = null;
    if (image != null && !image.isEmpty()) {
      imageUrl = storage.storeImage(image);
      course.setImageUrl(imageUrl);
    }

    // 코스 정보 업데이트
    course.setTitle(req.getTitle());
    course.setDescription(req.getDescription());
    course.setFreeTime(req.getFreeTime());
    course.setVisibility(req.getVisibility());

    // CoursePlace 업데이트
    List<UUID> placeIds = req.getPlaceIds();
    List<CoursePlace> coursePlaceList = IntStream.range(0, placeIds.size())
        .mapToObj(idx -> {
          UUID placeId = placeIds.get(idx);
          Place place = placeRepository.findByPlaceId(placeId)
              .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
          CoursePlaceId compositeId = new CoursePlaceId(course.getCourseId(), place.getPlaceId());
          return CoursePlace.builder()
              .coursePlaceId(compositeId)
              .course(course)
              .place(place)
              .sequence(idx + 1)
              .build();
        })
        .collect(Collectors.toList());

    // 기존 CoursePlace 삭제 후 새로 저장
    coursePlaceRepository.deleteAllByCourse(course);
    coursePlaceRepository.saveAll(coursePlaceList);

    entityManager.flush();
  }





  /**
   * 코스 dto를 빌드하는 공통 메서드
   */
  public CourseDto converToCourseDto(Course course) {
    return CourseDto.builder()
        .courseId(course.getCourseId())
        .nickName(course.getMember().getNickname())
        .title(course.getTitle())
        .description(course.getDescription())
        .freeTime(course.getFreeTime())
        .visibility(course.getVisibility())
        .imageUrl(course.getImageUrl())
        .placeDtos(course.getCoursePlaces().stream()
            .sorted(Comparator.comparing(CoursePlace::getSequence))
            .map(cp -> placeService.convertToPlaceDto(cp.getPlace()))
            .collect(Collectors.toList()))
        .likeCount(course.getLikeCount())
        .build();
  }
}
