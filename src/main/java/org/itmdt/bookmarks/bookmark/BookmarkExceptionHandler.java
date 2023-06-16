package org.itmdt.bookmarks.bookmark;

import org.itmdt.bookmarks.bookmark.exceptions.BookmarkNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BookmarkExceptionHandler {
    @ResponseBody
    @ExceptionHandler(BookmarkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String userNotFoundHandler(BookmarkNotFoundException ex) {
        return ex.getMessage();
    }
}
