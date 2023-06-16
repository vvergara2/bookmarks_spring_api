package org.itmdt.bookmarks.groupuser;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTagging;
import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.user.User;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
public class GroupUser {
    @EmbeddedId
    private GroupUserKey groupUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private Group group;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private Date joinDate;

    private boolean pending = true;

    private boolean inviteIgnored;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canAddBookmarks;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canRemoveBookmarks;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canInviteUsers;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canRemoveUsers;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canGrantAddBookmarksPermission;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canGrantRemoveBookmarksPermission;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canGrantInviteUsersPermission;

    @JsonView(GroupUserView.FromGroupUserOnly.class)
    private boolean canGrantRemoveUsersPermission;

    @PrePersist
    void preInsert() {
        if (this.joinDate == null) {
            this.joinDate = new Date();
        }
    }

    public GroupUser() {}

    public GroupUser(Group group, User user) {
        this.user = user;
        this.group = group;
        this.groupUserId = new GroupUserKey(user.getUserId(), group.getGroupId());
    }

    public GroupUser(Long userId, Long groupId) {
        this.groupUserId = new GroupUserKey(userId, groupId);
    }

    public GroupUserKey getGroupUserId() {
        return groupUserId;
    }

    public void setGroupUserId(GroupUserKey groupUserId) {
        this.groupUserId = groupUserId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isInviteIgnored() {
        return inviteIgnored;
    }

    public void setInviteIgnored(boolean inviteIgnored) {
        this.inviteIgnored = inviteIgnored;
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

    public void setAllOwnerPermissions() {
        this.setCanAddBookmarks(true);
        this.setCanRemoveBookmarks(true);
        this.setCanInviteUsers(true);
        this.setCanRemoveUsers(true);
        this.setCanGrantAddBookmarksPermission(true);
        this.setCanGrantRemoveBookmarksPermission(true);
        this.setCanGrantInviteUsersPermission(true);
        this.setCanGrantRemoveUsersPermission(true);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.groupUserId, this.user, this.group);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof GroupUser)) {
            return false;
        }

        GroupUser otherGroupUser = (GroupUser) obj;
        return Objects.equals(this.groupUserId, otherGroupUser.groupUserId)
                && Objects.equals(this.user, otherGroupUser.user)
                && Objects.equals(this.group, otherGroupUser.group);
    }

}
