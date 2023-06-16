package org.itmdt.bookmarks.tag.exceptions;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException() {
        super("tag-not-found");
    }
}
