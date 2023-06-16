package org.itmdt.bookmarks.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

public interface TagRepository extends JpaSpecificationExecutor<Tag>, JpaRepository<Tag, Long> {
    @Query("SELECT t from Tag t WHERE name LIKE :tagName AND creator_id = :creatorId")
    public Tag getTagByNameAndCreatorId(@Param("tagName") String tagName, @Param("creatorId") Long creatorId);

    @Query("SELECT t from Tag t WHERE name LIKE :tagName AND group_id = :groupId")
    public Tag getTagByNameAndGroupId(@Param("tagName") String tagName, @Param("groupId") Long groupId);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "group"
            }
    )
    @Query("SELECT t FROM Tag t WHERE tag_id = :tagId")
    public Tag findByIdAndFetchGroup(@Param("tagId") Long tagId);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "creator",
                    "group"
            }
    )
    Page<Tag> findAll(@Nullable Specification<Tag> spec, Pageable pageable);
}
