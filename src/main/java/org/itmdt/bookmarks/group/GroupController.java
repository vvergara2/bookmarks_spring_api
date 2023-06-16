package org.itmdt.bookmarks.group;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.user.UserDetails;
import org.itmdt.bookmarks.user.exceptions.UserInvalidSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
public class GroupController {
    @Autowired
    private GroupService groupService;

    @Secured("USER")
    @PostMapping("/groups")
    @JsonView(GroupView.FromGroupOnly.class)
    public Group newGroup(@RequestBody @Valid GroupCreateDTO newGroupDTO,
                          @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.newGroup(newGroupDTO, userDetails.getUser());
    }

    @Secured("USER")
    @GetMapping("/groups/{groupId}")
    @JsonView(GroupView.FromGroupOnly.class)
    public Group getGroupById(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getGroupById(groupId, userDetails.getUser());
    }

    @Secured("USER")
    @GetMapping("/groups/me")
    @JsonView(GroupView.FromGroupOnly.class)
    public List<Group> getLoggedInUserGroups(
            @RequestParam(defaultValue = "false") boolean canAddBookmarks,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getLoggedInUserGroups(canAddBookmarks, userDetails.getUser());
    }
}
