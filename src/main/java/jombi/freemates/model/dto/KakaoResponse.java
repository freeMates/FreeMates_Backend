package jombi.freemates.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoResponse {
  private Meta meta;
  private List<KakaoDocument> documents;

  @Getter
  public static class Meta {
    private int totalCount;
    private int pageableCount;
    private boolean isEnd;
  }
}
