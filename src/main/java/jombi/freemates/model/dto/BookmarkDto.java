package jombi.freemates.model.dto;

import java.util.List;
import java.util.UUID;
import jombi.freemates.model.constant.PinColor;
import jombi.freemates.model.constant.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkDto {

  private UUID bookmarkId;
  private String imageUrl;
  private String title;
  private String description;
  private PinColor pinColor;
  private Visibility visibility;
  private UUID memberId;
  private String nickname;
  private Long likeCount; // 좋아요 수

  private List<PlaceDto> placeDtos;


}
