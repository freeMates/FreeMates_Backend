package jombi.freemates.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jombi.freemates.model.constant.CategoryType;
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
   *
   * @param customUser 현재 로그인한 사용자 정보
   * @param req 코스 생성 요청 정보
   * @param image 코스 대표 이미지 (선택 사항)
   * @return 생성된 코스 정보 DTO
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

    // Course 엔티티 생성·저장 (쓰기 모드 트랜잭션)
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

    // placeIds 각각으로 Place 조회 → CoursePlace 엔티티 생성
    List<UUID> placeIds = req.getPlaceIds();
    List<CoursePlace> coursePlaceList = IntStream.range(0, placeIds.size())
        .mapToObj(idx -> {
          UUID placeId = placeIds.get(idx);

          // Place 조회
          Place place = placeRepository.findByPlaceId(placeId)
              .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

          // 복합키 생성
          CoursePlaceId compositeId = new CoursePlaceId(course.getCourseId(), place.getPlaceId());

          // CoursePlace 생성 (sequence = idx+1)
          return CoursePlace.builder()
              .coursePlaceId(compositeId)
              .course(course)
              .place(place)
              .sequence(idx + 1)
              .build();
        })
        .collect(Collectors.toList());

    // CoursePlace 엔티티 목록을 한 번에 저장
    coursePlaceRepository.saveAll(coursePlaceList);

    // DTO 변환: 저장한 coursePlaceList를 바로 사용한다.
    //    (course.getCoursePlaces()를 다시 쓰지 않아도 됨)
    List<CoursePlaceDto> coursePlaceDtos = coursePlaceList.stream()
        // 이미 순서대로 만들어 놓았기 때문에 별도 정렬 없어도 되지만,
        // 안전하게 시퀀스 정렬을 걸어줄 수도 있습니다.
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

    // 최종 응답용 DTO 생성
    return CourseDto.builder()
        .courseId(course.getCourseId())
        .nickName(member.getNickname())
        .title(course.getTitle())
        .description(course.getDescription())
        .freeTime(course.getFreeTime())
        .visibility(course.getVisibility())
        .imageUrl(course.getImageUrl())
        .placeIds(req.getPlaceIds())
        .coursePlaceDtos(coursePlaceDtos)
        .build();
  }


  /**
   *
   * 내 코스 목록 가져오기
   */

    @Transactional(readOnly = true)
    public List<CourseDto> getMyCourses(CustomUserDetails customUser) {
      // 회원 정보 꺼내기, 없으면 예외
      Member member = customUser.getMember();
      if (member == null) {
        throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
      }

      // 해당 회원이 만든 Course 리스트 가져오기
      List<Course> courses = courseRepository.findAllByMember(member);

      // Course → CourseDto로 매핑
      return courses.stream()
          .map(course -> {
            // CoursePlace 리스트에서 CoursePlaceDto 생성
            List<CoursePlaceDto> placeDtoList = course.getCoursePlaces().stream()
                // 필요 시 sequence(순서) 필드로 정렬하고 싶으면, Comparator.comparing(CoursePlace::getSequence) 적용
                .sorted(Comparator.comparing(CoursePlace::getSequence))
                .map(coursePlace -> {
                  Place place = coursePlace.getPlace();
                  return CoursePlaceDto.builder()
                      .placeName(place.getPlaceName())
                      .distance(place.getDistance())
                      .categoryType(place.getCategoryType())
                      .imageUrl(place.getImageUrl())
                      .tags(place.getTags())
                      .build();
                })
                .collect(Collectors.toList());

            // CoursePlace 리스트에서 Place ID만 따로 뽑아서 placeIds 생성
            List<UUID> placeIds = course.getCoursePlaces().stream()
                .sorted(Comparator.comparing(CoursePlace::getSequence))
                .map(coursePlace -> coursePlace.getPlace().getPlaceId())
                .collect(Collectors.toList());

            // 최종적으로 CourseDto 생성
            return CourseDto.builder()
                .courseId(course.getCourseId())
                .nickName(course.getMember().getNickname())
                .title(course.getTitle())
                .description(course.getDescription())
                .freeTime(course.getFreeTime())
                .visibility(course.getVisibility())
                .imageUrl(course.getImageUrl())
                .placeIds(placeIds)
                .coursePlaceDtos(placeDtoList)
                .build();
          })
          .collect(Collectors.toList());
    }

  /**
   * public인 코스 목록 가져오기
   */
  @Transactional(readOnly = true)
  public List<CourseDto> getCourses(Visibility visibility) {
    // 공개된 코스만 가져오기
    List<Course> courses = courseRepository.findAllByVisibility(visibility);

    // Course → CourseDto로 매핑
    return courses.stream()
        .map(course -> {
          // CoursePlace 리스트에서 CoursePlaceDto 생성
          List<CoursePlaceDto> placeDtoList = course.getCoursePlaces().stream()
              .sorted(Comparator.comparing(CoursePlace::getSequence))
              .map(coursePlace -> {
                Place place = coursePlace.getPlace();
                return CoursePlaceDto.builder()
                    .placeName(place.getPlaceName())
                    .distance(place.getDistance())
                    .categoryType(place.getCategoryType())
                    .imageUrl(place.getImageUrl())
                    .tags(place.getTags())
                    .build();
              })
              .collect(Collectors.toList());

          // CoursePlace 리스트에서 Place ID만 따로 뽑아서 placeIds 생성
          List<UUID> placeIds = course.getCoursePlaces().stream()
              .sorted(Comparator.comparing(CoursePlace::getSequence))
              .map(coursePlace -> coursePlace.getPlace().getPlaceId())
              .collect(Collectors.toList());

          // 최종적으로 CourseDto 생성
          return CourseDto.builder()
              .courseId(course.getCourseId())
              .nickName(course.getMember().getNickname())
              .title(course.getTitle())
              .description(course.getDescription())
              .freeTime(course.getFreeTime())
              .visibility(course.getVisibility())
              .imageUrl(course.getImageUrl())
              .placeIds(placeIds)
              .coursePlaceDtos(placeDtoList)
              .build();
        })
        .collect(Collectors.toList());
  }

}
