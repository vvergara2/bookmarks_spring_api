package org.itmdt.bookmarks.tag;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingView;
import org.itmdt.bookmarks.user.*;
import org.itmdt.bookmarks.user.exceptions.UserInvalidSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Arrays;

@RestController
public class TagController {
    @Autowired
    TagService tagService;

    private final String[] validSortOptions = {
            "tagId",
            "name",
            "creator",
            "createdDate",
            "lastUpdatedDate",
            "lastUseDate"
    };

    @Secured("USER")
    @GetMapping("/tags")
    @JsonView(BookmarkTaggingView.FromTagOnly.class)
    Page<Tag> getTags(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "") String search,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (sort != null && !Arrays.asList(validSortOptions).contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return tagService.getTags(sort, order, page, creatorId, groupId, search, userDetails.getUser());
    }

    @Secured("USER")
    @PutMapping("/tags/{tagId}")
    @JsonView(BookmarkTaggingView.FromTagOnly.class)
    Tag updateTag(@PathVariable Long tagId, @RequestBody @Valid TagUpdateDTO updateTagDTO,
                  @AuthenticationPrincipal UserDetails userDetails) {
        return tagService.updateTag(tagId, updateTagDTO, userDetails.getUser());
    }

    @Secured("USER")
    @GetMapping("/tags/{tagId}")
    @JsonView(BookmarkTaggingView.FromTagOnly.class)
    Tag getTagById(@PathVariable Long tagId, @AuthenticationPrincipal UserDetails userDetails) {
        return tagService.getTagById(tagId, userDetails.getUser());
    }

    @Secured("USER")
    @DeleteMapping("/tags/{tagId}")
    void deleteTag(@PathVariable Long tagId, @AuthenticationPrincipal UserDetails userDetails) {
        tagService.deleteTag(tagId, userDetails.getUser());
    }
}
