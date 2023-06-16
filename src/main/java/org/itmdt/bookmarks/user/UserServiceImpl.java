package org.itmdt.bookmarks.user;

import org.itmdt.bookmarks.Util;
import org.itmdt.bookmarks.user.exceptions.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Value("${bookmarks.verify_code_expiration_time}")
    private Integer VERIFY_CODE_EXPIRATION_TIME;
    @Value("${bookmarks.reset_password_code_expiration_time}")
    private Integer RESET_PASSWORD_CODE_EXPIRATION_TIME;
    @Value("${bookmarks.minimum_password_length}")
    private Integer MINIMUM_PASSWORD_LENGTH;


    @Value("${bookmarks.emails.demo_bypass}")
    private Boolean EMAIL_DEMO_BYPASS;
    @Value("${bookmarks.emails.from}")
    private String EMAILS_FROM;
    @Value("${bookmarks.emails.verify_user_route}")
    private String VERIFY_USER_ROUTE;
    @Value("${bookmarks.emails.reset_password_route}")
    private String RESET_PASSWORD_ROUTE;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepo;


    public void generateNewVerificationCode(User user, int expirationTimeSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, expirationTimeSeconds);

        user.setVerificationCode(Util.generateRandomUUIDString());
        user.setVerificationExpiryDate(calendar.getTime());
    }

    public void generateNewResetPasswordCode(User user, int expirationTimeSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, expirationTimeSeconds);

        user.setResetPasswordCode(Util.generateRandomUUIDString());
        user.setResetPasswordExpiryDate(calendar.getTime());
    }

//
//    @Override
//    public List<User> getAllUsers() {
//        return userRepo.findAll();
//    }

    @Override
    public String newUser(UserCreateDTO newUserDTO) {
        Date now = new Date();

        User existingUser = userRepo.getUserByEmail(newUserDTO.getEmail());
        if (existingUser != null
                && (existingUser.isVerified() || existingUser.getVerificationExpiryDate().after(now))) {
            throw new UserWithEmailExistsException();
        }

        existingUser = userRepo.getUserByUsername(newUserDTO.getUsername());
        if (existingUser != null
                && (existingUser.isVerified() || existingUser.getVerificationExpiryDate().after(now))) {
            throw new UserWithUsernameExistsException();
        }

        if (newUserDTO.getPassword().length() < MINIMUM_PASSWORD_LENGTH) {
            throw new UserPasswordTooShortException(MINIMUM_PASSWORD_LENGTH);
        }

        if (existingUser != null) {
            // a user with desired email or username exists but was not verified in time
            userRepo.delete(existingUser);
            userRepo.flush();
        }

        User newUser = new User();
        generateNewVerificationCode(newUser, VERIFY_CODE_EXPIRATION_TIME);

        modelMapper.map(newUserDTO, newUser);
        newUser.setPassword(passwordEncoder.encode(newUserDTO.getPassword()));

        newUser = userRepo.save(newUser);

//        logger.debug(
//                String.format("User %s created with verification code %s",
//                        newUser.getUsername(),
//                        newUser.getVerificationCode()
//                )
//        );

        if (EMAIL_DEMO_BYPASS) {
            return newUser.getVerificationCode();
        }

        // TODO configurable template
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAILS_FROM);
        message.setTo(newUser.getEmail());
        message.setSubject("Verify your Bookmarks account");
        message.setText("Visit " + RESET_PASSWORD_ROUTE
                + "?email=" + newUser.getEmail()
                + "&verifyCode=" + newUser.getVerificationCode());
        emailSender.send(message);

        return "";
    }

    @Override
    public void resetPassword(UserResetPasswordRequestDTO reqDTO) {
        User foundUser = userRepo.getUserByEmail(reqDTO.getEmail());
        if (foundUser == null) {
            throw new UserNotFoundException();
        }

        Date curDate = new Date();
        if (foundUser.getResetPasswordExpiryDate().after(curDate)) {
            // reset code still active
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        generateNewResetPasswordCode(foundUser, RESET_PASSWORD_CODE_EXPIRATION_TIME);
        userRepo.save(foundUser);

//        logger.debug(
//                String.format("User %s generated new reset password code %s",
//                        foundUser.getUsername(),
//                        foundUser.getResetPasswordCode()
//                )
//        );

        if (EMAIL_DEMO_BYPASS) {
            // don't return the code even in demo.
            // if for some reason EMAIL_DEMO_BYPASS is accidentally set, returning the code would allow changing the
            // password of any account
            return;
        }

        // TODO configurable template
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAILS_FROM);
        message.setTo(foundUser.getEmail());
        message.setSubject("Reset your Bookmarks account password");
        message.setText("Visit " + RESET_PASSWORD_ROUTE
                + "?email=" + foundUser.getEmail()
                + "&resetPasswordCode=" + foundUser.getResetPasswordCode());
        emailSender.send(message);
    }

    @Override
    public void updatePassword(UserResetPasswordDTO passwordDTO) {
        User foundUser = userRepo.getUserByEmail(passwordDTO.getEmail());
        if (foundUser == null) {
            throw new UserNotFoundException();
        }

        if (!foundUser.getResetPasswordCode().equals(passwordDTO.getResetPasswordCode())) {
            // verify code did not match
            throw new UserResetCodeDidNotMatchException();
        }

        Date curDate = new Date();
        if (foundUser.getResetPasswordExpiryDate().before(curDate)) {
            // reset code expired
            throw new UserResetCodeExpiredException();
        }

        if (passwordDTO.getNewPassword().length() < MINIMUM_PASSWORD_LENGTH) {
            // password not long enough
            throw new UserPasswordTooShortException(MINIMUM_PASSWORD_LENGTH);
        }

        foundUser.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        foundUser.setResetPasswordExpiryDate(new Date(0));
        userRepo.save(foundUser);

    }
//
//    @Override
//    public User getUserById(Long userId) {
//        return userRepo.findById(userId).orElseThrow(UserNotFoundException::new);
//    }

    @Override
    public void deleteUser(UserDeleteRequestDTO deleteRequestDTO, User requestingUser) {
        if (passwordEncoder.matches(deleteRequestDTO.getPassword(), requestingUser.getPassword())) {
            userRepo.deleteById(requestingUser.getUserId());
        }
    }

    @Override
    public void verifyUser(UserVerifyDTO verifyDTO) {
        User foundUser = userRepo.getUserByEmail(verifyDTO.getEmail());
        if (foundUser == null) {
            throw new UserNotFoundException();
        }

        if (foundUser.isVerified()) {
            return;
        }

        if (foundUser.getVerificationExpiryDate().before(new Date())) {
            throw new UserVerifyCodeExpiredException();
        }

        if (verifyDTO.getVerifyCode().equals(foundUser.getVerificationCode())) {
            foundUser.setVerified(true);
        } else {
            throw new UserVerifyCodeDidNotMatchException();
        }

        userRepo.save(foundUser);
    }
}
