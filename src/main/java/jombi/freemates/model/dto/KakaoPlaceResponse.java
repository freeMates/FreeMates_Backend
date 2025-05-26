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
public class KakaoPlaceResponse {
  private Meta meta;
  private List<KakaoPlaceDocumentResponse> documents;

  @Getter
  public static class Meta {
    private int totalCount;
    private int pageableCount;
    private boolean isEnd;
  }
}
