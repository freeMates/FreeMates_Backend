package jombi.freemates.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jombi.freemates.model.constant.PinColor;
import jombi.freemates.model.constant.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark extends BasePostgresEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID bookmarkId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member; // 즐겨찾기를 한 회원

  private String imageUrl; // 즐겨찾기한 장소의 이미지 URL

  private String title;

  private String description;

  @Enumerated(EnumType.STRING)
  private PinColor pinColor;

  @Enumerated(EnumType.STRING)
  private Visibility visibility;

  @ManyToMany
  @JoinTable(
      name = "bookmark_place",
      joinColumns = @JoinColumn(name = "bookmark_id"),
      inverseJoinColumns = @JoinColumn(name = "place_id")
  )
  private List<Place> places = new ArrayList<>(); // 즐겨찾기한 장소들



}
