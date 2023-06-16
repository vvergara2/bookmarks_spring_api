package org.itmdt.bookmarks.user;

import org.itmdt.bookmarks.user.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class UserExceptionHandler {
    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String userNotFoundHandler(UserNotFoundException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserInvalidSessionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userInvalidSessionHandler(UserInvalidSessionException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserPasswordTooShortException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String userPasswordTooShortHandler(UserPasswordTooShortException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserResetCodeDidNotMatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userResetCodeDidNotMatchHandler(UserResetCodeDidNotMatchException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserResetCodeExpiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userResetCodeExpiredHandler(UserResetCodeExpiredException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserVerifyCodeDidNotMatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userVerifyCodeDidNotMatchHandler(UserVerifyCodeDidNotMatchException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserVerifyCodeExpiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userVerifyCodeExpiredHandler(UserVerifyCodeExpiredException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserWithEmailExistsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userWithEmailExistsHandler(UserWithEmailExistsException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserWithUsernameExistsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String userWithUsernameExistsHandler(UserWithUsernameExistsException ex) {
        return ex.getMessage();
    }
}
