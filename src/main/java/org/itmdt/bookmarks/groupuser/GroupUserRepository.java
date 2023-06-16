package org.itmdt.bookmarks.groupuser;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUserKey> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "group"
            }
    )
    @Query("SELECT gu FROM GroupUser gu WHERE user_id = :userId AND pending = :pending AND inviteIgnored = :inviteIgnored")
    public List<GroupUser> getGroupUsersByUserId(@Param("userId") Long userId, @Param("pending") boolean pending, @Param("inviteIgnored") boolean inviteIgnored);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "user"
            }
    )
    @Query("SELECT gu FROM GroupUser gu WHERE group_id = :groupId AND pending = :pending")
    public List<GroupUser> getGroupUsersByGroupId(@Param("groupId") Long userId, @Param("pending") boolean pending);

    @Query("SELECT gu FROM GroupUser gu WHERE group_id = :groupId AND user_id = :userId")
    public GroupUser getGroupUser(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT gu FROM GroupUser gu WHERE group_id = :groupId AND user_id != :leavingUserId AND canRemoveUsers = true AND pending = false ORDER BY joinDate ASC")
    public GroupUser findOwnerSuccessor(@Param("groupId") Long groupId, @Param("leavingUserId") Long leavingUserId);

    // this is used if there are no other users in the group with canRemoveUsers = true
    @Query("SELECT gu FROM GroupUser gu WHERE group_id = :groupId AND user_id != :leavingUserId AND pending = false ORDER BY joinDate ASC")
    public GroupUser findOwnerSuccessorFallback(@Param("groupId") Long groupId, @Param("leavingUserId") Long leavingUserId);
}
