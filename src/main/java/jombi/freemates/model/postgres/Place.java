package jombi.freemates.model.postgres;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
  @Column(name = "place_id", updatable = false, nullable = false)
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
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "place_tags",
      joinColumns = @JoinColumn(name = "place_id")
  )
  @Builder.Default
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();

  // 프리메이트에서 사용하는 정보
  @Builder.Default
  private Long likeCount = 0L;

  @Builder.Default
  private Long viewCount = 0L;

  @Enumerated(EnumType.STRING)
  private CategoryType categoryType;
}
