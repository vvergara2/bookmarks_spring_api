package org.itmdt.bookmarks.user.exceptions;

public class UserInvalidSessionException extends RuntimeException {
    public UserInvalidSessionException() {
        super("user-session-invalid");
    }
}
