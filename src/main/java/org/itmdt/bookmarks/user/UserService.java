package org.itmdt.bookmarks.user;

import java.util.List;

public interface UserService {
//    List<User> getAllUsers();

    String newUser(UserCreateDTO newUserDTO);

    void resetPassword(UserResetPasswordRequestDTO reqDTO);

    void updatePassword(UserResetPasswordDTO passwordDTO);

//    User getUserById(Long userId);

    void deleteUser(UserDeleteRequestDTO deleteRequestDTO, User requestingUser);

    void verifyUser(UserVerifyDTO verifyDTO);
}
