package org.itmdt.bookmarks.tag;

import org.itmdt.bookmarks.bookmark.BookmarkRepository;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingRepository;
import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.group.GroupRepository;
import org.itmdt.bookmarks.groupuser.GroupUserRepository;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserLacksPermission;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserNotFoundException;
import org.itmdt.bookmarks.tag.exceptions.TagNotFoundException;
import org.itmdt.bookmarks.user.User;
import org.itmdt.bookmarks.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Objects;

import static org.itmdt.bookmarks.tag.TagSpecifications.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class TagServiceImpl implements TagService {
    @Value("${bookmarks.max_page_length}")
    private Integer MAX_PAGE_LENGTH;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BookmarkRepository bookmarkRepo;
    @Autowired
    private TagRepository tagRepo;
    @Autowired
    private BookmarkTaggingRepository taggingRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GroupUserRepository groupUserRepo;

    @Transactional
    @Override
    public Page<Tag> getTags(String sort, String order, Integer page, Long creatorId, Long groupId, String search, User requestingUser) {
        Sort requestedSort = Sort.by("lastUpdatedDate").descending();
        if (sort != null && order != null) {
            boolean ascending = (order.equalsIgnoreCase("asc"));
            requestedSort = ascending ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        }

        Pageable requestConfig = PageRequest.of(page, MAX_PAGE_LENGTH, requestedSort);

        Specification<Tag> query = where(null);

        if (groupId != null && groupId > 0) {
            GroupUser groupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
            if (groupUser == null || groupUser.isPending()) {
                // requesting user is not part of group
                throw new GroupUserNotFoundException();
            }

            if (creatorId != null) {
                query = query.and(belongsToUser(creatorId));
            }

            query = query.and(belongsToGroup(groupId));
        } else {
            query = query.and(belongsToUser(requestingUser.getUserId())).and(doesNotBelongToGroup());
        }

        // filter by search phrase
        if (!search.isEmpty()) {
            query = query.and(containsPhrase(search));
        }

        return tagRepo.findAll(query, requestConfig);
    }

    @Transactional
    @Override
    public Tag updateTag(Long tagId, TagUpdateDTO tagUpdateDTO, User requestingUser) {
        Tag foundTag = tagRepo.findById(tagId).orElseThrow(TagNotFoundException::new);
        modelMapper.map(tagUpdateDTO, foundTag);
        foundTag.setLastUpdatedDate(new Date());

        Group tagGroup = foundTag.getGroup();
        if (tagGroup != null) {
            GroupUser groupUser = groupUserRepo.getGroupUser(tagGroup.getGroupId(), requestingUser.getUserId());
            if (groupUser == null || groupUser.isPending()) {
                throw new GroupUserNotFoundException();
            }

            if (!groupUser.getCanRemoveBookmarks()) {
                throw new GroupUserLacksPermission();
            }
        } else {
            // don't allow updating another user's tag
            if (!Objects.equals(requestingUser.getUserId(), foundTag.getCreator().getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }

        tagRepo.save(foundTag);

        return foundTag;
    }

    @Transactional
    @Override
    public Tag getTagById(Long tagId, User requestingUser) {
        Tag foundTag = tagRepo.findByIdAndFetchGroup(tagId);

        if (foundTag == null) {
            throw new TagNotFoundException();
        }

        Group tagGroup = foundTag.getGroup();
        if (tagGroup == null) {
            if (!Objects.equals(requestingUser.getUserId(), foundTag.getCreator().getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else {
            GroupUser groupUser = groupUserRepo.getGroupUser(tagGroup.getGroupId(), requestingUser.getUserId());
            if (groupUser == null || groupUser.isPending()) {
                throw new GroupUserNotFoundException();
            }
        }


        return foundTag;
    }

    @Transactional
    @Override
    public void deleteTag(Long tagId, User requestingUser) {
        Tag foundTag = tagRepo.findById(tagId).orElseThrow(TagNotFoundException::new);

        Group tagGroup = foundTag.getGroup();
        if (tagGroup != null) {
            GroupUser groupUser = groupUserRepo.getGroupUser(tagGroup.getGroupId(), requestingUser.getUserId());
            if (groupUser == null || groupUser.isPending()) {
                throw new GroupUserNotFoundException();
            }

            if (!groupUser.getCanRemoveBookmarks()) {
                throw new GroupUserLacksPermission();
            }
        } else {
            if (!Objects.equals(requestingUser.getUserId(), foundTag.getCreator().getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }

        tagRepo.deleteById(tagId);
    }
}
