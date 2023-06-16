package org.itmdt.bookmarks.user.exceptions;

public class UserVerifyCodeExpiredException extends RuntimeException {
    public UserVerifyCodeExpiredException() {
        super("user-verify-code-expired");
    }
}
