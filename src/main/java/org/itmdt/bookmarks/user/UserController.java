package org.itmdt.bookmarks.user;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.BookmarksViews;
import org.itmdt.bookmarks.user.exceptions.UserInvalidSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/users")
    @JsonView(BookmarksViews.Public.class)
    String newUser(@RequestBody UserCreateDTO newUserDTO) {
        return userService.newUser(newUserDTO);
    }

    @PostMapping("/users/reset_password")
    void resetPassword(@RequestBody UserResetPasswordRequestDTO reqDTO) {
        userService.resetPassword(reqDTO);
    }

    @PutMapping("/users/reset_password")
    void updatePassword(@RequestBody UserResetPasswordDTO passwordDTO) {
        userService.updatePassword(passwordDTO);
    }

    @PostMapping("/users/verify")
    void verifyUser(@RequestBody UserVerifyDTO verifyDTO) {
        userService.verifyUser(verifyDTO);
    }

//    @Secured("USER")
//    @GetMapping("/users")
//    @JsonView(BookmarksViews.Public.class)
//    List<User> getAllUsers() {
//        return userService.getAllUsers();
//    }

//    @Secured("USER")
//    @GetMapping("/users/{userId}")
//    @JsonView(BookmarksViews.Public.class)
//    User getUserById(@PathVariable Long userId) {
//        return userService.getUserById(userId);
//    }

    @Secured("USER")
    @GetMapping("/users/me")
    @JsonView(BookmarksViews.Public.class)
    User getLoggedInUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getUser();
    }

    @Secured("USER")
    @PostMapping("/users/deleteme")
    void deleteUser(@RequestBody UserDeleteRequestDTO deleteRequestDTO, @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(deleteRequestDTO, userDetails.getUser());
    }
}
