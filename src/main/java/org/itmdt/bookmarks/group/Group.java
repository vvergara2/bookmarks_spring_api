package org.itmdt.bookmarks.group;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.BookmarksViews;
import org.itmdt.bookmarks.bookmark.Bookmark;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.tag.Tag;
import org.itmdt.bookmarks.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "[group]")
public class Group {
    @Id
    @Column(name = "group_id")
    @GeneratedValue
    @JsonView(BookmarksViews.Public.class)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @JsonView(GroupView.FromGroupOnly.class)
    private @NotNull User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonView(GroupView.FromGroupOnly.class)
    private @NotNull User owner;

    @JsonView(BookmarksViews.Public.class)
    private String name;

//    @JsonView(GroupView.FromGroupOnly.class)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<GroupUser> groupUsers;

//    @JsonView(GroupView.FromGroupOnly.class)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Tag> tags;

//    @JsonView(GroupView.FromGroupOnly.class)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Bookmark> bookmarks;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<GroupUser> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(Set<GroupUser> groupUsers) {
        this.groupUsers = groupUsers;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(Set<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.groupId, this.name, this.creator);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Group)) {
            return false;
        }

        Group group = (Group) obj;
        return Objects.equals(this.groupId, group.groupId)
                && Objects.equals(this.creator, group.creator)
                && Objects.equals(this.name, group.name);
    }
}
