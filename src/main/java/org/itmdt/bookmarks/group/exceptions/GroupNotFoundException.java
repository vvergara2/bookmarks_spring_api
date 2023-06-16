package org.itmdt.bookmarks.group.exceptions;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {
        super("group-not-found");
    }
}
