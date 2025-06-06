package jombi.freemates.repository;

import java.util.List;
import java.util.UUID;
import jombi.freemates.model.postgres.BookmarkPlace;
import jombi.freemates.model.postgres.id.BookmarkPlaceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkPlaceRepository extends JpaRepository<BookmarkPlace, BookmarkPlaceId> {
  // 특정 bookmarkId로 연결된 Place 목록 미리 뽑고 싶다면
  List<BookmarkPlace> findByBookmarkBookmarkId(UUID bookmarkId);


  boolean existsByBookmarkMemberMemberIdAndPlacePlaceId(UUID memberId, UUID placeId);

  // 특정 placeId가 포함된 Bookmark 목록 뽑기
  List<BookmarkPlace> findByPlacePlaceId(UUID placeId);

  Boolean existsByPlacePlaceId(UUID placeId);
}