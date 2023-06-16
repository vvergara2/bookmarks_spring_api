package org.itmdt.bookmarks.groupuser;

public class GroupUserUpdateDTO {
    private Long userId;
    
    private Long groupId;

    private boolean canAddBookmarks;

    private boolean canRemoveBookmarks;

    private boolean canInviteUsers;

    private boolean canRemoveUsers;

    private boolean canGrantAddBookmarksPermission;

    private boolean canGrantRemoveBookmarksPermission;

    private boolean canGrantInviteUsersPermission;

    private boolean canGrantRemoveUsersPermission;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public boolean getCanAddBookmarks() {
        return canAddBookmarks;
    }

    public void setCanAddBookmarks(boolean canAddBookmarks) {
        this.canAddBookmarks = canAddBookmarks;
    }

    public boolean getCanRemoveBookmarks() {
        return canRemoveBookmarks;
    }

    public void setCanRemoveBookmarks(boolean canRemoveBookmarks) {
        this.canRemoveBookmarks = canRemoveBookmarks;
    }

    public boolean getCanInviteUsers() {
        return canInviteUsers;
    }

    public void setCanInviteUsers(boolean canInviteUsers) {
        this.canInviteUsers = canInviteUsers;
    }

    public boolean getCanRemoveUsers() {
        return canRemoveUsers;
    }

    public void setCanRemoveUsers(boolean canRemoveUsers) {
        this.canRemoveUsers = canRemoveUsers;
    }

    public boolean getCanGrantAddBookmarksPermission() {
        return canGrantAddBookmarksPermission;
    }

    public void setCanGrantAddBookmarksPermission(boolean canGrantAddBookmarksPermission) {
        this.canGrantAddBookmarksPermission = canGrantAddBookmarksPermission;
    }

    public boolean getCanGrantRemoveBookmarksPermission() {
        return canGrantRemoveBookmarksPermission;
    }

    public void setCanGrantRemoveBookmarksPermission(boolean canGrantRemoveBookmarksPermission) {
        this.canGrantRemoveBookmarksPermission = canGrantRemoveBookmarksPermission;
    }

    public boolean getCanGrantInviteUsersPermission() {
        return canGrantInviteUsersPermission;
    }

    public void setCanGrantInviteUsersPermission(boolean canGrantInviteUsersPermission) {
        this.canGrantInviteUsersPermission = canGrantInviteUsersPermission;
    }

    public boolean getCanGrantRemoveUsersPermission() {
        return canGrantRemoveUsersPermission;
    }

    public void setCanGrantRemoveUsersPermission(boolean canGrantRemoveUsersPermission) {
        this.canGrantRemoveUsersPermission = canGrantRemoveUsersPermission;
    }
}
