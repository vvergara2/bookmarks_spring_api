package org.itmdt.bookmarks.user.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("user-not-found");
    }
}
