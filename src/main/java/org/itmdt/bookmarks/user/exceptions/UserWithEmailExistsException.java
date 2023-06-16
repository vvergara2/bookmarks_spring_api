package org.itmdt.bookmarks.user.exceptions;

public class UserWithEmailExistsException extends RuntimeException {
    public UserWithEmailExistsException() {
        super("user-with-email-exists");
    }
}
