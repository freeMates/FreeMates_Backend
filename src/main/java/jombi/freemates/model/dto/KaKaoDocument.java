package jombi.freemates.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KaKaoDocument {

  private String id;

  private String placeName;

  private String categoryName;

  private String categoryGroupCode;

  private String categoryGroupName;

  private String phone;

  private String addressName;

  private String roadAddressName;

  private String x;

  private String y;

  private String placeUrl;

  private String distance;

}
