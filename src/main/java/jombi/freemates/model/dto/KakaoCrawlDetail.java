package jombi.freemates.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCrawlDetail {
  private String imgUrl;
  // 상세설명
  private String description;
  // 편의시설
  private String amenities;


}
