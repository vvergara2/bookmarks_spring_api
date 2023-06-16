package org.itmdt.bookmarks.group;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long>,
        JpaSpecificationExecutor<Group> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "groupUsers"
            }
    )
    List<Group> findAll(@Nullable Specification<Group> spec);
}
