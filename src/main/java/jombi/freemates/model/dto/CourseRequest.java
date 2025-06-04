package jombi.freemates.model.dto;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
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
public class CourseRequest {
  @NotNull
  @Schema(description = "코스 제목")
  private String title;

  @Schema(description = "코스 설명")
  private String description;

  @NotNull
  @Schema(description = "코스 소요 예상 시간(분 단위)")
  private Integer freeTime;

  @NotNull
  @Schema(description = "공개 여부 (ENUM: PUBLIC 또는 PRIVATE)")
  private Visibility visibility;

  @NotNull
  @Schema(description = "이 코스에 포함할 장소들의 ID 목록 (순서대로 진행 순서를 결정한다면 순서를 보존)")
  private List<UUID> placeIds;

}
