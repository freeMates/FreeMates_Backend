package jombi.freemates.model.dto;

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
public class BookmarkResponse {

  private String imageUrl;
  private String title;
  private String description;
  private PinColor pinColor;
  private Visibility visibility;
  // --- 아래 필드 추가 ---
  private UUID memberId;
  private String nickname;

}
