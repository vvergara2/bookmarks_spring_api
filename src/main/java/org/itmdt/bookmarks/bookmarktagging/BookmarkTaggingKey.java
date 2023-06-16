package org.itmdt.bookmarks.bookmarktagging;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BookmarkTaggingKey implements Serializable {
    @Column(name = "bookmark_id")
    Long bookmarkId;

    @Column(name = "tag_id")
    Long tagId;

    public BookmarkTaggingKey() {}

    public BookmarkTaggingKey(Long bookmarkId, Long tagId) {
        this.bookmarkId = bookmarkId;
        this.tagId = tagId;
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bookmarkId, this.tagId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof BookmarkTaggingKey)) {
            return false;
        }

        BookmarkTaggingKey bookmarkTagging = (BookmarkTaggingKey) obj;
        return Objects.equals(this.bookmarkId, bookmarkTagging.bookmarkId)
                && Objects.equals(this.tagId, bookmarkTagging.tagId);
    }
}
