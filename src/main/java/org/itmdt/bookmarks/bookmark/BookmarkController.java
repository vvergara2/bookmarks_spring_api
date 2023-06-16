package org.itmdt.bookmarks.bookmark;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.bookmarktagging.*;
import org.itmdt.bookmarks.user.UserDetails;
import org.itmdt.bookmarks.user.exceptions.UserInvalidSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.*;

@RestController
public class BookmarkController {
    @Autowired
    private BookmarkService bookmarkService;

    private final String[] validSortOptions = {
            "bookmarkId",
            "creator",
            "url",
            "displayTitle",
            "createdDate",
            "lastUpdatedDate"
    };

    @Secured("USER")
    @GetMapping("/bookmarks")
    @JsonView(BookmarkTaggingView.FromBookmarkOnly.class)
    Page<Bookmark> getBookmarks(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "") String tagIds,
            @RequestParam(defaultValue = "") String search,
            @AuthenticationPrincipal UserDetails userDetails
        ) {
        if (sort != null && !Arrays.asList(validSortOptions).contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return bookmarkService.getBookmarks(sort, order, page, creatorId, groupId, tagIds, search, userDetails.getUser());
    }

    @Secured("USER")
    @PostMapping("/bookmarks")
    @JsonView(BookmarkTaggingView.FromBookmarkOnly.class)
    Bookmark newBookmark(@RequestBody @Valid BookmarkCreateDTO newBookmarkDTO,
                         @AuthenticationPrincipal UserDetails userDetails) {
        return bookmarkService.createBookmark(newBookmarkDTO, userDetails.getUser());
    }

    @Secured("USER")
    @PutMapping("/bookmarks/{bookmarkId}")
    @JsonView(BookmarkTaggingView.FromBookmarkOnly.class)
    Bookmark updateBookmark(@PathVariable Long bookmarkId, @RequestBody @Valid BookmarkUpdateDTO updateDTO,
                            @AuthenticationPrincipal UserDetails userDetails) {
        return bookmarkService.updateBookmark(bookmarkId, updateDTO, userDetails.getUser());
    }

    @Secured("USER")
    @GetMapping("/bookmarks/{bookmarkId}")
    @JsonView(BookmarkTaggingView.FromBookmarkOnly.class)
    Bookmark getBookmarkById(@PathVariable Long bookmarkId, @AuthenticationPrincipal UserDetails userDetails) {
        return bookmarkService.getBookmarkById(bookmarkId, userDetails.getUser());
    }

    @Secured("USER")
    @DeleteMapping("/bookmarks/{bookmarkId}")
    void deleteBookmark(@PathVariable Long bookmarkId, @AuthenticationPrincipal UserDetails userDetails) {
        bookmarkService.deleteBookmark(bookmarkId, userDetails.getUser());
    }

}
