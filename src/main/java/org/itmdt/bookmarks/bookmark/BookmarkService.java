package org.itmdt.bookmarks.bookmark;

import org.itmdt.bookmarks.user.User;
import org.springframework.data.domain.Page;

public interface BookmarkService {
    Page<Bookmark> getBookmarks(String sort, String order, Integer page, Long creatorId, Long groupId,
                                String tagIds, String search, User requestingUser);

    Bookmark createBookmark(BookmarkCreateDTO newBookmarkDTO, User requestingUser);

    Bookmark updateBookmark(Long bookmarkId, BookmarkUpdateDTO updateDTO, User requestingUser);

    Bookmark getBookmarkById(Long bookmarkId, User requestingUser);

    void deleteBookmark(Long bookmarkId, User requestingUser);
}
