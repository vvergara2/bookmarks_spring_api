package org.itmdt.bookmarks.user.exceptions;

public class UserWithUsernameExistsException extends RuntimeException {
    public UserWithUsernameExistsException() {
        super("user-with-username-exists");
    }
}
