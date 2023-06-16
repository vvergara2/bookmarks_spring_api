package org.itmdt.bookmarks.bookmark;

import org.itmdt.bookmarks.Util;
import org.itmdt.bookmarks.bookmark.exceptions.BookmarkNotFoundException;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTagging;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingRepository;
import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.group.exceptions.GroupNotFoundException;
import org.itmdt.bookmarks.group.GroupRepository;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.groupuser.GroupUserRepository;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserLacksPermission;
import org.itmdt.bookmarks.groupuser.exceptions.GroupUserNotFoundException;
import org.itmdt.bookmarks.tag.Tag;
import org.itmdt.bookmarks.tag.TagRepository;
import org.itmdt.bookmarks.user.User;
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

import java.util.*;

import static org.itmdt.bookmarks.bookmark.BookmarkSpecifications.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class BookmarkServiceImpl implements BookmarkService {
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
    private GroupUserRepository groupUserRepo;

    @Transactional
    private Set<Tag> parseDesiredTags(List<BookmarkFormTagDTO> desiredTags, User requestingUser, Group desiredGroup) {
        Set<Tag> finalTagSet = new HashSet<>();
        if (desiredTags == null) {
            return finalTagSet;
        }

        for (BookmarkFormTagDTO tagData : desiredTags) {
            if (tagData.getTagId() == -1) {
                String trimmedTagName = tagData.getName().trim();
                if (trimmedTagName.isEmpty()) {
                    // just ignore empty tags
                    continue;
                }

                // if a tag is associated with a group, it should be found by name and groupid
                Tag existingTag = null;
                if (desiredGroup != null) {
                    existingTag = tagRepo.getTagByNameAndGroupId(trimmedTagName, desiredGroup.getGroupId());
                } else {
                    existingTag = tagRepo.getTagByNameAndCreatorId(trimmedTagName, requestingUser.getUserId());
                }

                if (existingTag == null) {
                    Tag newTag = new Tag();
                    newTag.setCreator(requestingUser);
                    newTag.setName(trimmedTagName);
                    if (desiredGroup != null) {
                        newTag.setGroup(desiredGroup);
                    }

                    finalTagSet.add(newTag);
                } else {
                    existingTag.setLastUseDate(new Date());
                    finalTagSet.add(existingTag);
                }
            } else {
                Tag existingTag = tagRepo.findByIdAndFetchGroup(tagData.getTagId());
                if (existingTag == null) {
                    continue;
                }

                if (desiredGroup != null) {
                    if (!Objects.equals(existingTag.getGroup().getGroupId(), desiredGroup.getGroupId())) {
                        // reject: existing tag belongs to a different group than our bookmark
                        continue;
                    }
                } else {
                    if (existingTag.getGroup() != null) {
                        // reject: existing tag which has this ID is associated with a group, but our bookmark is not
                        continue;
                    }

                    if (!Objects.equals(existingTag.getCreator().getUserId(), requestingUser.getUserId())) {
                        // reject: trying to use someone else's tag on a private bookmark
                        continue;
                    }
                }

                existingTag.setLastUseDate(new Date());
                finalTagSet.add(existingTag);
            }
        }

        return finalTagSet;
    }

    @Transactional
    @Override
    public Page<Bookmark> getBookmarks(String sort, String order, Integer page, Long creatorId, Long groupId,
                                       String tagIds, String search, User requestingUser) {
        Sort requestedSort = Sort.by("lastUpdatedDate").descending();
        if (sort != null && order != null) {
            boolean ascending = order.equalsIgnoreCase("asc");
            requestedSort = ascending ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        }

        Pageable requestConfig = PageRequest.of(page, MAX_PAGE_LENGTH, requestedSort);

        Specification<Bookmark> query = where(null);

        if (groupId != null && groupId > 0) {
            GroupUser groupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());
            if (groupUser == null) {
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

        // filter by tag presence
        if (!tagIds.isEmpty()) {
            // Tags here don't need to be checked for correct group/user membership, because only valid relationships
            // will be created using the new bookmark route. So if a user tries to filter by a tag that doesn't belong
            // to their selected group filter, there simply won't be any results.
            try {
                List<Long> tagIdsConverted = Util.numberStringToLongArray(tagIds);
                query = query.and(hasTags(tagIdsConverted));
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        return bookmarkRepo.findAll(query, requestConfig);
    }

    @Transactional
    @Override
    public Bookmark createBookmark(BookmarkCreateDTO newBookmarkDTO, User requestingUser) {
        Bookmark newBookmark = new Bookmark();
        modelMapper.map(newBookmarkDTO, newBookmark);

        newBookmark.setCreator(requestingUser);

        Group foundGroup = null;
        GroupUser groupUser = null;

        if (newBookmarkDTO.getGroupId() != null && newBookmarkDTO.getGroupId() > 0) {
            Long groupId = newBookmarkDTO.getGroupId();

            foundGroup = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
            groupUser = groupUserRepo.getGroupUser(groupId, requestingUser.getUserId());

            if (groupUser == null || groupUser.isPending()) {
                throw new GroupUserNotFoundException();
            }

            if (!groupUser.getCanAddBookmarks()) {
                throw new GroupUserLacksPermission();
            }

            newBookmark.setGroup(foundGroup);
        } else {
            newBookmark.setGroup(null);
        }

        // tag handling
        Set<Tag> desiredTags = parseDesiredTags(newBookmarkDTO.getDesiredTags(), requestingUser, foundGroup);

        List<Tag> finalTags = tagRepo.saveAll(desiredTags);
        Bookmark finalBookmark = bookmarkRepo.save(newBookmark);

        Set<BookmarkTagging> newTaggings = new HashSet<>();
        for (Tag tag : finalTags) {
            BookmarkTagging newTagging = new BookmarkTagging(finalBookmark, tag);
            newTaggings.add(newTagging);
        }
        taggingRepo.saveAll(newTaggings);

        // fill in taggings here so that we can show them to user
        finalBookmark.setTaggings(newTaggings);

        // need to retrieve bookmark again to fill in tag info
        return finalBookmark;
    }

    @Transactional
    @Override
    public Bookmark updateBookmark(Long bookmarkId, BookmarkUpdateDTO updateDTO, User requestingUser) {
        Bookmark foundBookmark = bookmarkRepo.findById(bookmarkId).orElseThrow(BookmarkNotFoundException::new);

        // don't allow updating another user's bookmark
        if (!Objects.equals(requestingUser.getUserId(), foundBookmark.getCreator().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        modelMapper.map(updateDTO, foundBookmark);
        foundBookmark.setLastUpdatedDate(new Date());

        // tag handling
        Set<Tag> desiredTags = parseDesiredTags(updateDTO.getDesiredTags(), requestingUser, foundBookmark.getGroup());

        List<Tag> finalTags = tagRepo.saveAll(desiredTags);
        Bookmark finalBookmark = bookmarkRepo.save(foundBookmark);

        taggingRepo.deleteBookmarkTaggings(finalBookmark.getBookmarkId());
        Set<BookmarkTagging> newTaggings = new HashSet<>();
        for (Tag tag : finalTags) {
            BookmarkTagging newTagging = new BookmarkTagging(finalBookmark, tag);
            newTaggings.add(newTagging);
        }
        taggingRepo.saveAll(newTaggings);

        return foundBookmark;
    }

    @Transactional
    @Override
    public Bookmark getBookmarkById(Long bookmarkId, User requestingUser) {
        Bookmark foundBookmark = bookmarkRepo.findByIdAndFetchGroup(bookmarkId);

        if (foundBookmark == null) {
            throw new BookmarkNotFoundException();
        }

        if (foundBookmark.getGroup() == null) {
            // don't let a user see another user's private bookmark
            if (!Objects.equals(requestingUser.getUserId(), foundBookmark.getCreator().getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else {
            GroupUser existingGroupUser = groupUserRepo.getGroupUser(foundBookmark.getGroup().getGroupId(), requestingUser.getUserId());
            if (existingGroupUser == null) {
                throw new GroupUserNotFoundException();
            }
        }

        return foundBookmark;
    }

    @Transactional
    @Override
    public void deleteBookmark(Long bookmarkId, User requestingUser) {
        Bookmark foundBookmark = bookmarkRepo.findById(bookmarkId).orElseThrow(BookmarkNotFoundException::new);
        Group bookmarkGroup = foundBookmark.getGroup();

        if (bookmarkGroup == null) {
            if (!Objects.equals(requestingUser.getUserId(), foundBookmark.getCreator().getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else {
            // to remove a bookmark belonging to a group, either the requesting user needs to have created the bookmark,
            // or the user needs to have remove bookmark permissions for that group
            GroupUser requestingGroupUser = groupUserRepo.getGroupUser(bookmarkGroup.getGroupId(), requestingUser.getUserId());
            if (!Objects.equals(foundBookmark.getCreator().getUserId(), requestingUser.getUserId())
                    && !requestingGroupUser.getCanRemoveBookmarks()) {
                throw new GroupUserLacksPermission();
            }
        }

        bookmarkRepo.deleteById(bookmarkId);
    }
}
