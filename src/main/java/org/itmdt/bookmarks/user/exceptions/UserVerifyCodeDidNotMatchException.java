package org.itmdt.bookmarks.user.exceptions;

public class UserVerifyCodeDidNotMatchException extends RuntimeException {
    public UserVerifyCodeDidNotMatchException() {
        super("user-verify-code-did-not-match");
    }
}
