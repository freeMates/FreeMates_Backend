package jombi.freemates.model.postgres;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Place extends BasePostgresEntity{

  // 카카오에서 받아 오는 정보
  @Id
  private String id;

  private String addressName;

  private String categoryGroupCode;

  private String distance;

  private String phone;

  private String placeName;

  private String placeUrl;

  private String roadAddressName;

  private String x;

  private String y;

  // 네이버 크롤링한 정보

  private String imgUrl;

  private String description;

  private String amenities;

  // 프리메이트에서 사용하는 정보

  private Long likeCnt;

  private Long viewCnt;
}
