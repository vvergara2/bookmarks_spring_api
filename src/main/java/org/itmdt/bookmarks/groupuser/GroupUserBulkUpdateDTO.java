package org.itmdt.bookmarks.groupuser;

import java.util.List;

public class GroupUserBulkUpdateDTO {
    private List<GroupUserUpdateDTO> groupUserUpdateList;

    public List<GroupUserUpdateDTO> getGroupUserUpdateList() {
        return groupUserUpdateList;
    }

    public void setGroupUserUpdateList(List<GroupUserUpdateDTO> groupUserUpdateList) {
        this.groupUserUpdateList = groupUserUpdateList;
    }
}
