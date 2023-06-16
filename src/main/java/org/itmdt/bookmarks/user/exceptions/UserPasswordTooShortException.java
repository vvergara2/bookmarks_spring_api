package org.itmdt.bookmarks.user.exceptions;

public class UserPasswordTooShortException extends RuntimeException {
    public UserPasswordTooShortException(Integer passwordLengthRequirement) {
        super("user-password-too-short");
    }
}
