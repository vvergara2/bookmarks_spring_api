package org.itmdt.bookmarks.group;

import org.itmdt.bookmarks.group.exceptions.GroupNotFoundException;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.groupuser.GroupUserRepository;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserNotFoundException;
import org.itmdt.bookmarks.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.itmdt.bookmarks.group.GroupSpecifications.userCanAddBookmarks;
import static org.itmdt.bookmarks.group.GroupSpecifications.userIsMember;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private GroupUserRepository groupUserRepo;

    @Transactional
    @Override
    public Group newGroup(GroupCreateDTO newGroupDTO, User requestingUser) {
        Group newGroup = new Group();
        modelMapper.map(newGroupDTO, newGroup);

        newGroup.setCreator(requestingUser);
        newGroup.setOwner(requestingUser);
        groupRepo.save(newGroup);

        GroupUser groupAdminUser = new GroupUser(newGroup, requestingUser);

        groupAdminUser.setPending(false);
        groupAdminUser.setAllOwnerPermissions();

        groupUserRepo.save(groupAdminUser);

        return newGroup;
    }

    @Override
    public Group getGroupById(Long groupId, User requestingUser) {
        GroupUser groupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
        if (groupUser == null) {
            throw new GroupUserNotFoundException();
        }

        return groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
    }

    @Override
    public List<Group> getLoggedInUserGroups(boolean canAddBookmarksOnly, User requestingUser) {
        Specification<Group> query = where(userIsMember(requestingUser.getUserId()));
        if (canAddBookmarksOnly) {
            query = where(userCanAddBookmarks(requestingUser.getUserId()));
        }
        return groupRepo.findAll(query);
    }
}
