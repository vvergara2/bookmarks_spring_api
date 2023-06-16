package org.itmdt.bookmarks.bookmark;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class BookmarkUpdateDTO {
    @NotBlank
    private String url;
    private String displayTitle;
    private String description;
    private List<BookmarkFormTagDTO> desiredTags;

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

    public List<BookmarkFormTagDTO> getDesiredTags() {
        return desiredTags;
    }

    public void setDesiredTags(List<BookmarkFormTagDTO> desiredTags) {
        this.desiredTags = desiredTags;
    }
}
