package org.itmdt.bookmarks.groupuser;

import org.itmdt.bookmarks.groupuser.exceptions.GroupUserLacksPermission;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GroupUserExceptionHandler {
    @ResponseBody
    @ExceptionHandler(GroupUserLacksPermission.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String groupUserLacksPermissionHandler(GroupUserLacksPermission ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(GroupUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String groupUserNotFoundHandler(GroupUserNotFoundException ex) {
        return ex.getMessage();
    }
}
