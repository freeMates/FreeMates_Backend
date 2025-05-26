package jombi.freemates.model.dto;

import java.util.ArrayList;
import java.util.List;
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
  // 태그들
  @Builder.Default
  private List<String> tags = new ArrayList<>();


}
