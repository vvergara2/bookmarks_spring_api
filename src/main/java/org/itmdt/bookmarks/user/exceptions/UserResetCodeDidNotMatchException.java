package org.itmdt.bookmarks.user.exceptions;

public class UserResetCodeDidNotMatchException extends RuntimeException {
    public UserResetCodeDidNotMatchException() {
        super("user-reset-code-did-not-match");
    }
}
