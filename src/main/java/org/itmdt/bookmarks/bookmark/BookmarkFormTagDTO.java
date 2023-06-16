package org.itmdt.bookmarks.bookmark;

import javax.validation.constraints.NotBlank;

/**
 * This DTO is used for tags submitted via form in a JSON array
 */

public class BookmarkFormTagDTO {
    private long tagId;
    @NotBlank
    private String name;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
