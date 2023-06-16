package org.itmdt.bookmarks.groupuser;

import org.itmdt.bookmarks.user.User;

import java.util.List;

public interface GroupUserService {
    GroupUser updateGroupUser(GroupUserUpdateDTO updateDTO, User requestingUser);

    void bulkUpdateGroupUser(GroupUserBulkUpdateDTO bulkUpdateDTO, User requestingUser);

    void createInvite(Long groupId, User requestingUser, Long recipientId);

    void rescindInvite(Long groupId, User requestingUser, Long recipientId);

    void acceptInvite(Long groupId, User requestingUser);

    void denyInvite(Long groupId, User requestingUser);

    void ignoreInvite(Long groupId, User requestingUser);

    void leaveGroup(Long groupId, User requestingUser, Long successorId);

    void removeUser(Long groupId, Long removeUserId, User requestingUser);

    List<GroupUser> getGroupUsersForLoggedInUser(User requestingUser, boolean pending, boolean ignored);

    List<GroupUser> getGroupUsersForGroup(Long groupId, User requestingUser, boolean pending);
}
