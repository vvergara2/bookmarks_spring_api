package org.itmdt.bookmarks.groupuser;

import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.group.exceptions.GroupNotFoundException;
import org.itmdt.bookmarks.group.GroupRepository;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserLacksPermission;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserNotFoundException;
import org.itmdt.bookmarks.user.User;
import org.itmdt.bookmarks.user.exceptions.UserNotFoundException;
import org.itmdt.bookmarks.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class GroupUserServiceImpl implements GroupUserService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GroupUserRepository groupUserRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private UserRepository userRepo;

    @Transactional
    @Override
    public GroupUser updateGroupUser(GroupUserUpdateDTO updateDTO, User requestingUser) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(updateDTO.getGroupId(), requestingUser.getUserId());
        if (requestingGroupUser == null || requestingGroupUser.isPending()) {
            // requesting user is not a member of the group
            throw new GroupUserNotFoundException();
        }

        GroupUser targetGroupUser = groupUserRepo.getGroupUser(updateDTO.getGroupId(), updateDTO.getUserId());
        if (targetGroupUser == null || targetGroupUser.isPending()) {
            throw new GroupUserNotFoundException();
        }

        User groupOwner = requestingGroupUser.getGroup().getOwner();

        if (Objects.equals(requestingUser.getUserId(), updateDTO.getUserId())) {
            // don't allow changing one's own permissions
            // this silently fails because 403 cancels the entire batch
            return targetGroupUser;
        }

        // detect if permissions are changed, and then check if the requesting user has
        // been granted permission to change them

        // group owner can do anything other than modify their own permissions
        if (!Objects.equals(groupOwner.getUserId(), requestingUser.getUserId())) {
            if (updateDTO.getCanAddBookmarks() != targetGroupUser.getCanAddBookmarks()
                    && !requestingGroupUser.getCanGrantAddBookmarksPermission()) {
                throw new GroupUserLacksPermission();
            }

            if (updateDTO.getCanRemoveBookmarks() != targetGroupUser.getCanRemoveBookmarks()
                    && !requestingGroupUser.getCanGrantAddBookmarksPermission()) {
                throw new GroupUserLacksPermission();
            }

            if (updateDTO.getCanInviteUsers() != targetGroupUser.getCanInviteUsers()
                    && !requestingGroupUser.getCanGrantInviteUsersPermission()) {
                throw new GroupUserLacksPermission();
            }

            if (updateDTO.getCanRemoveUsers() != targetGroupUser.getCanRemoveUsers()
                    && !requestingGroupUser.getCanGrantRemoveUsersPermission()) {
                throw new GroupUserLacksPermission();
            }

            // only the owner can modify grant perms
            if (updateDTO.getCanGrantAddBookmarksPermission() != targetGroupUser.getCanGrantAddBookmarksPermission()) {
                throw new GroupUserLacksPermission();
            }

            if (updateDTO.getCanGrantRemoveBookmarksPermission() != targetGroupUser.getCanGrantRemoveBookmarksPermission()) {
                throw new GroupUserLacksPermission();
            }

            if (updateDTO.getCanGrantInviteUsersPermission() != targetGroupUser.getCanGrantInviteUsersPermission()) {
                throw new GroupUserLacksPermission();
            }

            if (updateDTO.getCanGrantRemoveUsersPermission() != targetGroupUser.getCanGrantRemoveUsersPermission()) {
                throw new GroupUserLacksPermission();
            }
        }

        modelMapper.map(updateDTO, targetGroupUser);

        return groupUserRepo.save(targetGroupUser);
    }

    @Transactional
    @Override
    public void bulkUpdateGroupUser(GroupUserBulkUpdateDTO bulkUpdateDTO, User requestingUser) {
        List<GroupUserUpdateDTO> updates = bulkUpdateDTO.getGroupUserUpdateList();
        for (GroupUserUpdateDTO update : updates) {
            updateGroupUser(update, requestingUser);
        }
    }

    @Transactional
    @Override
    public void createInvite(Long groupId, User requestingUser, Long recipientId) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null || requestingGroupUser.isPending()) {
            throw new GroupUserNotFoundException();
        }

        if (!requestingGroupUser.getCanInviteUsers()) {
            throw new GroupUserLacksPermission();
        }

        GroupUser targetGroupUser = groupUserRepo.getGroupUser(groupId, recipientId);
        if (targetGroupUser != null) {
            return;
        }

        Group group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
        User recipient = userRepo.findById(recipientId).orElseThrow(UserNotFoundException::new);

        GroupUser newGroupUser = new GroupUser(group, recipient);

        groupUserRepo.save(newGroupUser);
    }

    @Transactional
    @Override
    public void rescindInvite(Long groupId, User requestingUser, Long recipientId) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null || requestingGroupUser.isPending()) {
            throw new GroupUserNotFoundException();
        }

        if (!requestingGroupUser.getCanInviteUsers()) {
            throw new GroupUserLacksPermission();
        }

        GroupUser targetGroupUser = groupUserRepo.getGroupUser(groupId, recipientId);
        if (targetGroupUser == null || !targetGroupUser.isPending()) {
            // can't rescind invite of someone who has already accepted
            return;
        }

        groupUserRepo.delete(targetGroupUser);
    }

    @Transactional
    @Override
    public void acceptInvite(Long groupId, User requestingUser) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null) {
            throw new GroupUserNotFoundException();
        }

        if (!requestingGroupUser.isPending()) {
            // user already accepted invite
            return;
        }

        requestingGroupUser.setPending(false);

        groupUserRepo.save(requestingGroupUser);
    }

    @Transactional
    @Override
    public void denyInvite(Long groupId, User requestingUser) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null) {
            throw new GroupUserNotFoundException();
        }

        if (!requestingGroupUser.isPending()) {
            // user already accepted invite
            return;
        }

        groupUserRepo.delete(requestingGroupUser);
    }

    @Transactional
    @Override
    public void ignoreInvite(Long groupId, User requestingUser) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null) {
            throw new GroupUserNotFoundException();
        }

        if (!requestingGroupUser.isPending()) {
            // user already accepted invite
            return;
        }

        requestingGroupUser.setInviteIgnored(true);

        groupUserRepo.save(requestingGroupUser);
    }

    @Transactional
    @Override
    public void leaveGroup(Long groupId, User requestingUser, Long successorId) {
        Group group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null) {
            throw new GroupUserNotFoundException();
        }

        if (Objects.equals(group.getOwner().getUserId(), requestingUser.getUserId())) {
            // the user leaving the group is the owner and needs to "transfer power"
            GroupUser preferredSuccessor = null;
            GroupUser defaultSuccessor = null;
            GroupUser fallbackSuccessor = null;
            boolean haveSuccessor = false;

            if (successorId != null) {
                preferredSuccessor = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
            }

            if (preferredSuccessor == null) {
                defaultSuccessor = groupUserRepo.findOwnerSuccessor(groupId, requestingUser.getUserId());
            } else {
                haveSuccessor = true;
                group.setOwner(preferredSuccessor.getUser());
                preferredSuccessor.setAllOwnerPermissions();
                groupUserRepo.save(preferredSuccessor);
                groupRepo.save(group);
            }

            if (!haveSuccessor) {
                if (defaultSuccessor == null) {
                    fallbackSuccessor = groupUserRepo.findOwnerSuccessorFallback(groupId, requestingUser.getUserId());
                } else {
                    haveSuccessor = true;
                    group.setOwner(defaultSuccessor.getUser());
                    defaultSuccessor.setAllOwnerPermissions();
                    groupUserRepo.save(defaultSuccessor);
                    groupRepo.save(group);
                }
            }

            if (!haveSuccessor) {
                if (fallbackSuccessor != null) {
                    haveSuccessor = true;
                    group.setOwner(fallbackSuccessor.getUser());
                    fallbackSuccessor.setAllOwnerPermissions();
                    groupUserRepo.save(fallbackSuccessor);
                    groupRepo.save(group);
                }
            }

            if (!haveSuccessor) {
                // group is empty and needs to be deleted
                groupRepo.delete(group);
                return;
            }
        }

        groupUserRepo.delete(requestingGroupUser);
    }

    @Transactional
    @Override
    public void removeUser(Long groupId, Long removeUserId, User requestingUser) {
        Group group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null || requestingGroupUser.isPending()) {
            throw new GroupUserNotFoundException();
        }

        if (!requestingGroupUser.getCanRemoveUsers()) {
            throw new GroupUserLacksPermission();
        }

        GroupUser removeGroupUser = groupUserRepo.getGroupUser(groupId, removeUserId);
        if (removeGroupUser == null) {
            throw new GroupUserNotFoundException();
        }

        if (!Objects.equals(requestingUser.getUserId(), group.getOwner().getUserId())
                && removeGroupUser.getCanRemoveUsers()) {
            // only the group owner can remove another member who has remove permissions
            throw new GroupUserLacksPermission();
        }

        groupUserRepo.delete(removeGroupUser);
    }

    @Override
    public List<GroupUser> getGroupUsersForLoggedInUser(User requestingUser, boolean pending, boolean ignored) {
        return groupUserRepo.getGroupUsersByUserId(requestingUser.getUserId(), pending, ignored);
    }

    @Override
    public List<GroupUser> getGroupUsersForGroup(Long groupId, User requestingUser, boolean pending) {
        GroupUser requestingGroupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (requestingGroupUser == null || requestingGroupUser.isPending()) {
            throw new GroupUserNotFoundException();
        }

        if (pending && !requestingGroupUser.getCanGrantInviteUsersPermission()) {
            // only users with invite perms can see pending invites
            throw new GroupUserLacksPermission();
        }

        return groupUserRepo.getGroupUsersByGroupId(groupId, pending);

    }
}
