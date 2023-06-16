package org.itmdt.bookmarks.user.exceptions;

public class UserResetCodeExpiredException extends RuntimeException {
    public UserResetCodeExpiredException() {
        super("user-reset-code-expired");
    }
}
