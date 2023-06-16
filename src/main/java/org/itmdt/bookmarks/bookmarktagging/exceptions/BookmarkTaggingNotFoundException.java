package org.itmdt.bookmarks.bookmarktagging.exceptions;

public class BookmarkTaggingNotFoundException extends RuntimeException {
    public BookmarkTaggingNotFoundException() {
        super("bookmark-tagging-not-found");
    }
}
