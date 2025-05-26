package jombi.freemates.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import jombi.freemates.model.constant.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.checkerframework.common.aliasing.qual.Unique;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class Place extends BasePostgresEntity{

  // 카카오에서 받아 오는 정보
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID placeId;

  @Column(unique = true)
  private String kakaoPlaceId;

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

  private String imageUrl;
  // 한줄 소개
  private String introText;
  // 상세설명
  private String description;

  // 프리메이트에서 사용하는 정보
  @Builder.Default
  private Long likeCount = 0L;

  @Builder.Default
  private Long viewCount = 0L;

  @Enumerated(EnumType.STRING)
  private CategoryType categoryType;
}
