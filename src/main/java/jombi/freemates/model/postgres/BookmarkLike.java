package jombi.freemates.model.postgres;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jombi.freemates.model.postgres.id.BookmarkLikeId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkLike {
  @EmbeddedId
  private BookmarkLikeId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("memberId")
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookmarkId")
  @JoinColumn(name = "bookmark_id", nullable = false)
  private Bookmark bookmark;}
