package org.itmdt.bookmarks.tag;

import org.itmdt.bookmarks.user.User;
import org.springframework.data.domain.Page;

public interface TagService {
    Page<Tag> getTags(String sort, String order, Integer page, Long creatorId, Long groupId, String search, User requestingUser);

    Tag updateTag(Long tagId, TagUpdateDTO tagUpdateDTO, User requestingUser);

    Tag getTagById(Long tagId, User requestingUser);

    void deleteTag(Long tagId, User requestingUser);


}
