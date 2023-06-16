package org.itmdt.bookmarks.tag;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.BookmarksViews;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTagging;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingView;
import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
public class Tag {
    @Id
    @Column(name = "tag_id")
    @GeneratedValue
    @JsonView(BookmarksViews.Public.class)
    private Long tagId;

    @JsonView(BookmarksViews.Public.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @JsonView(BookmarksViews.Public.class)
    private @NotNull String name;
    @JsonView(BookmarksViews.Public.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private @NotNull User creator;

    @JsonView(BookmarksViews.Public.class)
    private Date createdDate;
    @JsonView(BookmarksViews.Public.class)
    private Date lastUpdatedDate;
    @JsonView(BookmarksViews.Public.class)
    private Date lastUseDate;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<BookmarkTagging> taggings;

    public Tag() {}

    public Tag(String name, User creator) {
        this.name = name;
        this.creator = creator;
    }

    @PrePersist
    void preInsert() {
        if (this.createdDate == null) {
            this.createdDate = new Date();
        }

        if (this.lastUpdatedDate == null) {
            this.lastUpdatedDate = new Date();
        }

        if (this.lastUseDate == null) {
            this.lastUseDate = new Date();
        }
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Date getLastUseDate() {
        return lastUseDate;
    }

    public void setLastUseDate(Date lastUseDate) {
        this.lastUseDate = lastUseDate;
    }

    public Set<BookmarkTagging> getTaggings() {
        return taggings;
    }

    public void setTaggings(Set<BookmarkTagging> taggings) {
        this.taggings = taggings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tagId, this.name, this.creator);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Tag)) {
            return false;
        }

        Tag tag = (Tag) obj;
        return Objects.equals(this.tagId, tag.tagId)
                && Objects.equals(this.name, tag.name)
                && Objects.equals(this.creator, tag.creator);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
