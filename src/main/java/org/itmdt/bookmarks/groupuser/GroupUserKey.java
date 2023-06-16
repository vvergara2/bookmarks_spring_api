package org.itmdt.bookmarks.groupuser;

import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingKey;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupUserKey implements Serializable {
    @Column(name = "user_id")
    Long userId;

    @Column(name = "group_id")
    Long groupId;

    public GroupUserKey() {}

    public GroupUserKey(Long userId, Long groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(this.userId, this.groupId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof GroupUserKey)) {
            return false;
        }

        GroupUserKey groupUser = (GroupUserKey) obj;
        return Objects.equals(this.userId, groupUser.userId)
                && Objects.equals(this.groupId, groupUser.groupId);
    }
}
