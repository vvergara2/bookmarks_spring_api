package org.itmdt.bookmarks.groupuser.exceptions;

public class GroupUserLacksPermission extends RuntimeException {
    public GroupUserLacksPermission() {
        super("group-user-lacks-permission");
    }
}
