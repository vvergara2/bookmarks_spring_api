package org.itmdt.bookmarks.bookmark;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.BookmarksViews;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTagging;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingView;
import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
public class Bookmark {
    @Id
    @GeneratedValue
    @Column(name = "bookmark_id")
    @JsonView(BookmarksViews.Public.class)
    private Long bookmarkId;

    @JsonView(BookmarksViews.Public.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @JsonView(BookmarksViews.Public.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private @NotNull User creator;
    @JsonView(BookmarksViews.Public.class)
    private @NotNull String url;

    @JsonView(BookmarksViews.Public.class)
    private String displayTitle;
    @JsonView(BookmarksViews.Public.class)
    @Size(max=500)
    private String description;
    @JsonView(BookmarksViews.Public.class)
    private Date createdDate;
    @JsonView(BookmarksViews.Public.class)
    private Date lastUpdatedDate;

    @OneToMany(mappedBy = "bookmark", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonView(BookmarkTaggingView.FromBookmarkOnly.class)
    private Set<BookmarkTagging> taggings;

    //TODO FTS data column

    public Bookmark() {}

    public Bookmark(User creator, String url) {
        this.creator = creator;
        this.url = url;
    }

    @PrePersist
    void preInsert() {
        if (this.displayTitle == null || this.displayTitle.equals("")) {
            this.displayTitle = url;
        }

        if (this.description == null) {
            this.description = "";
        }

        if (this.createdDate == null) {
            this.createdDate = new Date();
        }

        if (this.lastUpdatedDate == null) {
            this.lastUpdatedDate = new Date();
        }
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Set<BookmarkTagging> getTaggings() {
        return taggings;
    }

    public void setTaggings(Set<BookmarkTagging> taggings) {
        this.taggings = taggings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bookmarkId, this.creator, this.url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Bookmark)) {
            return false;
        }

        Bookmark bookmark = (Bookmark) obj;
        return Objects.equals(this.bookmarkId, bookmark.bookmarkId)
                && Objects.equals(this.creator, bookmark.creator)
                && Objects.equals(this.url, bookmark.url);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
