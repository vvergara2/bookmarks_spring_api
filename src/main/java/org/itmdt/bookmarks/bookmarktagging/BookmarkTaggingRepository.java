package org.itmdt.bookmarks.bookmarktagging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BookmarkTaggingRepository extends JpaRepository<BookmarkTagging, BookmarkTaggingKey> {
    @Transactional
    @Modifying
    @Query("DELETE FROM BookmarkTagging WHERE bookmark_id = :bookmarkId")
    public void deleteBookmarkTaggings(@Param("bookmarkId") Long bookmarkId);
}
