package jombi.freemates.model.dto;

import java.util.List;
import java.util.UUID;
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
public class PlaceDto {
  private UUID placeId;
  private String placeName;
  private String introText;
  private String addressName;
  private String imageUrl;
  private List<String> tags;
  private CategoryType categoryType;
  private Long likeCount;
  private Long viewCount;
  private String distance;



  }