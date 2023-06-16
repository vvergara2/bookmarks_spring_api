package org.itmdt.bookmarks.group;

import org.itmdt.bookmarks.group.exceptions.GroupNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GroupExceptionHandler {
    @ResponseBody
    @ExceptionHandler(GroupNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String groupNotFoundHandler(GroupNotFoundException ex) {
        return ex.getMessage();
    }
}
