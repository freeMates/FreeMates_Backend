package jombi.freemates.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jombi.freemates.model.postgres.Bookmark;
import jombi.freemates.model.postgres.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
  List<Bookmark> findAllByMember(Member member);

  Optional<Bookmark> findByBookmarkIdAndMember(UUID bookmarkId, Member member);
}
