package org.itmdt.bookmarks.groupuser;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.user.UserDetails;
import org.itmdt.bookmarks.user.exceptions.UserInvalidSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupUserController {
    @Autowired
    private GroupUserService groupUserService;

//    @Secured("USER")
//    @PutMapping("/groupusers/update")
//    @JsonView(GroupUserView.FromGroupUserOnly.class)
//    GroupUser updateGroupUser(@RequestBody GroupUserUpdateDTO updateDTO,
//                              @AuthenticationPrincipal UserDetails userDetails) {
//        return groupUserService.updateGroupUser(updateDTO, userDetails.getUser());
//    }

    @Secured("USER")
    @PutMapping("/groupusers/bulkupdate")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void bulkUpdateGroupUser(@RequestBody GroupUserBulkUpdateDTO bulkUpdateDTO,
                             @AuthenticationPrincipal UserDetails userDetails) {
        groupUserService.bulkUpdateGroupUser(bulkUpdateDTO, userDetails.getUser());
    }

    @Secured("USER")
    @PostMapping("/groupusers/invite")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void createInvite(
            @RequestParam Long groupId,
            @RequestParam Long recipientId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.createInvite(groupId, userDetails.getUser(), recipientId);
    }

    @Secured("USER")
    @PostMapping("/groupusers/rescindinvite")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void rescindInvite(
            @RequestParam Long groupId,
            @RequestParam Long recipientId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.rescindInvite(groupId, userDetails.getUser(), recipientId);
    }

    @Secured("USER")
    @PostMapping("/groupusers/acceptinvite")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void acceptInvite(
            @RequestParam Long groupId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.acceptInvite(groupId, userDetails.getUser());
    }

    @Secured("USER")
    @PostMapping("/groupusers/denyinvite")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void denyInvite(
            @RequestParam Long groupId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.denyInvite(groupId, userDetails.getUser());
    }

    @Secured("USER")
    @PostMapping("/groupusers/ignoreinvite")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void ignoreInvite(
            @RequestParam Long groupId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.ignoreInvite(groupId, userDetails.getUser());
    }

    @Secured("USER")
    @PostMapping("/groupusers/leavegroup")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void leaveGroup(
            @RequestParam Long groupId,
            @RequestParam(required = false) Long successorId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.leaveGroup(groupId, userDetails.getUser(), successorId);
    }

    @Secured("USER")
    @PostMapping("/groupusers/removeuser")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    void removeUser(
            @RequestParam Long groupId,
            @RequestParam Long removeUserId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        groupUserService.removeUser(groupId, removeUserId, userDetails.getUser());
    }


    @Secured("USER")
    @GetMapping("/groupusers/me")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    List<GroupUser> getGroupUsersForLoggedInUser(
            @RequestParam(defaultValue = "false") boolean pending,
            @RequestParam(defaultValue = "false") boolean ignored,
            @AuthenticationPrincipal UserDetails userDetails
        ) {
        return groupUserService.getGroupUsersForLoggedInUser(userDetails.getUser(), pending, ignored);
    }

    @Secured("USER")
    @GetMapping("/groupusers/group/{groupId}")
    @JsonView(GroupUserView.FromGroupUserOnly.class)
    List<GroupUser> getGroupUsersForGroup(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "false") boolean pending,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return groupUserService.getGroupUsersForGroup(groupId, userDetails.getUser(), pending);
    }


}
