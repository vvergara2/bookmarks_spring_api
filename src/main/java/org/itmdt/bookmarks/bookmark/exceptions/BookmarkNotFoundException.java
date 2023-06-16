package org.itmdt.bookmarks.bookmark.exceptions;

public class BookmarkNotFoundException extends RuntimeException {
    public BookmarkNotFoundException() {
        super("bookmark-not-found");
    }
}
