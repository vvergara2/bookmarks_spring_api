package org.itmdt.bookmarks.bookmarktagging;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.BookmarksViews;
import org.itmdt.bookmarks.bookmark.Bookmark;
import org.itmdt.bookmarks.tag.Tag;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
public class BookmarkTagging {
    @EmbeddedId
    private BookmarkTaggingKey bookmarkTaggingId;

    @ManyToOne
    @MapsId("bookmarkId")
    @JoinColumn(name = "bookmark_id")
    @JsonView(BookmarkTaggingView.FromTagOnly.class)
    private Bookmark bookmark;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    @JsonView(BookmarkTaggingView.FromBookmarkOnly.class)
    private Tag tag;

    @JsonView(BookmarksViews.Public.class)
    private Date createdDate;

    @PrePersist
    void preInsert() {
        if (this.createdDate == null) {
            this.createdDate = new Date();
        }
    }

    public BookmarkTagging() {}

    public BookmarkTagging(Bookmark bookmark, Tag tag) {
        this.bookmark = bookmark;
        this.tag = tag;
        this.bookmarkTaggingId = new BookmarkTaggingKey(bookmark.getBookmarkId(), tag.getTagId());
    }

    public BookmarkTagging(Long bookmarkId, Long tagId) {
        this.bookmarkTaggingId = new BookmarkTaggingKey(bookmarkId, tagId);
    }

    public BookmarkTaggingKey getBookmarkTaggingId() {
        return bookmarkTaggingId;
    }

    public void setBookmarkTaggingId(BookmarkTaggingKey bookmarkTaggingId) {
        this.bookmarkTaggingId = bookmarkTaggingId;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    public void setBookmark(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bookmarkTaggingId, this.bookmark, this.tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof BookmarkTagging)) {
            return false;
        }

        BookmarkTagging otherBookmark = (BookmarkTagging) obj;
        return Objects.equals(this.bookmarkTaggingId, otherBookmark.bookmarkTaggingId)
                && Objects.equals(this.bookmark, otherBookmark.bookmark)
                && Objects.equals(this.tag, otherBookmark.tag);
    }
}
