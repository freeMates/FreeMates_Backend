package jombi.freemates.model.postgres;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jombi.freemates.model.postgres.id.BookmarkPlaceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookmark_place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkPlace {

  @EmbeddedId
  private BookmarkPlaceId bookmarkPlaceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookmarkId")
  @JoinColumn(name = "bookmark_id", nullable = false)
  private Bookmark bookmark;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("placeId")
  @JoinColumn(name = "place_id", nullable = false)
  private Place place;

}
