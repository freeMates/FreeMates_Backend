package jombi.freemates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import jombi.freemates.model.constant.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
  @Schema(description = "생성된 코스 ID")
  private UUID courseId;

  @Schema(description = "코스를 만든 회원 닉네임")
  private String nickName;

  @Schema(description = "코스 제목")
  private String title;

  @Schema(description = "코스 설명")
  private String description;

  @Schema(description = "코스 예상 소요 시간(분)")
  private Integer freeTime;

  @Schema(description = "코스 공개 여부")
  private Visibility visibility;

  @Schema(description = "이미지 URL (업로드한 경우)")
  private String imageUrl;


  @Schema(description = "해당 코스에 포함된 장소들의 ID 리스트")
  private List<UUID> placeIds;

  @Schema(description = "해당 코스에 포함된 장소들의 상세 정보 리스트")
  private List<CoursePlaceDto> coursePlaceDtos;

}
