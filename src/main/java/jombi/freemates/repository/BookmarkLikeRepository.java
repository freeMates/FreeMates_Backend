package jombi.freemates.repository;

import jombi.freemates.model.postgres.BookmarkLike;
import jombi.freemates.model.postgres.id.BookmarkLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkLikeRepository extends JpaRepository<BookmarkLike, BookmarkLikeId> {
  boolean existsById(BookmarkLikeId id);
}
