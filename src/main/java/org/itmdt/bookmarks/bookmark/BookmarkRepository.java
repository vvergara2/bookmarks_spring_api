package org.itmdt.bookmarks.bookmark;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

public interface BookmarkRepository extends PagingAndSortingRepository<Bookmark, Long>,
        JpaSpecificationExecutor<Bookmark> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "group"
            }
    )
    @Query("SELECT b FROM Bookmark b WHERE bookmark_id = :bookmarkId")
    public Bookmark findByIdAndFetchGroup(@Param("bookmarkId") Long bookmarkId);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "creator",
                    "taggings",
                    "taggings.tag"
            }
    )
    Page<Bookmark> findAll(@Nullable Specification<Bookmark> spec, Pageable pageable);
}
