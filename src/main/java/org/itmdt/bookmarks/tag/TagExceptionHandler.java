package org.itmdt.bookmarks.tag;

import org.itmdt.bookmarks.tag.exceptions.TagNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class TagExceptionHandler {
    @ResponseBody
    @ExceptionHandler(TagNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String tagNotFoundHandler(TagNotFoundException ex) {
        return ex.getMessage();
    }
}
