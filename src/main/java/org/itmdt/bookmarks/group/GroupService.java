package org.itmdt.bookmarks.group;

import org.itmdt.bookmarks.user.User;

import java.util.List;

public interface GroupService {
    Group newGroup(GroupCreateDTO groupDTO, User requestingUser);
    Group getGroupById(Long groupId, User requestingUser);

    List<Group> getLoggedInUserGroups(boolean canAddBookmarksOnly, User requestingUser);

}
