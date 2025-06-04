package jombi.freemates.model.dto;

import java.util.ArrayList;
import java.util.List;
import jombi.freemates.model.constant.CategoryType;
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
public class PlaceDto {
  private String placeName;
  private String roadAddressName;
  private String imageUrl;
  private String introText;
  private List<String> tags = new ArrayList<>();
  private CategoryType categoryType;

}
