package org.itmdt.bookmarks.groupuser.exceptions;

public class GroupUserNotFoundException extends RuntimeException {
    public GroupUserNotFoundException() {
        super("group-user-not-found");
    }
}
