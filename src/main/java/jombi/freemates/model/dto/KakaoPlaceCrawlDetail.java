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
public class KakaoPlaceCrawlDetail {
  private String imageUrl;
  // 한줄 소개
  private String introText;
  // 상세정보
  private String description;


}
