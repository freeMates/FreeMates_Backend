package jombi.freemates.model.dto;

import java.util.List;
import jombi.freemates.model.constant.CategoryType;
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
public class CoursePlaceDto {
  private String placeName;
  private String distance;
  private CategoryType categoryType;
  private String imageUrl;
  private List<String> tags;

}
