package jombi.freemates.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jombi.freemates.model.constant.Visibility;
import jombi.freemates.model.dto.CoursePlaceDto;
import jombi.freemates.model.dto.CourseRequest;
import jombi.freemates.model.dto.CourseDto;
import jombi.freemates.model.dto.CustomUserDetails;
import jombi.freemates.model.postgres.Course;
import jombi.freemates.model.postgres.CoursePlace;
import jombi.freemates.model.postgres.Member;
import jombi.freemates.model.postgres.Place;
import jombi.freemates.model.postgres.id.CoursePlaceId;
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
    }

    // Course 엔티티 생성·저장
    Course course = courseRepository.save(
        Course.builder()
            .member(member)
            .title(req.getTitle())
            .description(req.getDescription())
            .freeTime(req.getFreeTime())
            .visibility(req.getVisibility())
            .imageUrl(imageUrl)
            .build()
    );

    // placeIds 각각으로 Place 조회 → CoursePlace 생성
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

    // 바로 DTO 변환
    return buildCourseDto(course, req.getPlaceIds(), coursePlaceList, member.getNickname());
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
        .map(course -> buildCourseDto(course, extractPlaceIds(course.getCoursePlaces()), course.getCoursePlaces(), member.getNickname()))
        .collect(Collectors.toList());
  }

  /**
   * public인 코스 목록 가져오기
   */
  @Transactional(readOnly = true)
  public Page<CourseDto> getCourses(Visibility visibility, Pageable pageable) {
    Page<Course> coursePage = courseRepository.findAllByVisibility(visibility, pageable);

    // Page<Course> → Page<CourseDto>로 변환
    return coursePage.map(course -> {
      // buildCourseDto(...)는 앞서 리팩토링한 공통 메서드
      List<UUID> placeIds = extractPlaceIds(course.getCoursePlaces());
      List<CoursePlace> coursePlaces = course.getCoursePlaces();
      String nickName = course.getMember().getNickname();
      return buildCourseDto(course, placeIds, coursePlaces, nickName);
    });
  }

  /**
   * Course → CourseDto 변환 공통 로직
   *
   */
  private CourseDto buildCourseDto(
      Course course,
      List<UUID> placeIds,
      List<CoursePlace> coursePlaces,
      String nickName
  ) {
    // CoursePlaceDto 목록 생성
    List<CoursePlaceDto> placeDtoList = coursePlaces.stream()
        .sorted(Comparator.comparing(CoursePlace::getSequence))
        .map(cp -> {
          Place p = cp.getPlace();
          return CoursePlaceDto.builder()
              .placeName(p.getPlaceName())
              .distance(p.getDistance())
              .categoryType(p.getCategoryType())
              .imageUrl(p.getImageUrl())
              .tags(p.getTags())
              .build();
        })
        .collect(Collectors.toList());

    return CourseDto.builder()
        .courseId(course.getCourseId())
        .nickName(nickName)
        .title(course.getTitle())
        .description(course.getDescription())
        .freeTime(course.getFreeTime())
        .visibility(course.getVisibility())
        .imageUrl(course.getImageUrl())
        .placeIds(placeIds)
        .coursePlaceDtos(placeDtoList)
        .build();
  }

  /**
   * 주어진 CoursePlace 목록에서 Place ID를 순서에 맞춰 추출하여 반환
   */
  private List<UUID> extractPlaceIds(List<CoursePlace> coursePlaces) {
    return coursePlaces.stream()
        .sorted(Comparator.comparing(CoursePlace::getSequence))
        .map(cp -> cp.getPlace().getPlaceId())
        .collect(Collectors.toList());
  }
}
