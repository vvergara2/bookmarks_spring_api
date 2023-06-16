package org.itmdt.bookmarks;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import net.minidev.json.JSONObject;
import org.itmdt.bookmarks.bookmark.Bookmark;
import org.itmdt.bookmarks.bookmark.BookmarkRepository;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTagging;
import org.itmdt.bookmarks.bookmarktagging.BookmarkTaggingRepository;
import org.itmdt.bookmarks.group.Group;
import org.itmdt.bookmarks.group.GroupRepository;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.groupuser.GroupUserRepository;
import org.itmdt.bookmarks.tag.Tag;
import org.itmdt.bookmarks.tag.TagRepository;
import org.itmdt.bookmarks.user.User;
import org.itmdt.bookmarks.user.UserRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BookmarksApplication.class, webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {
    private long authedUserId;
    private long otherUserId;
    private long dummyUserId;
    private long authedUserGroupId;
    private long otherUserGroupId;
    private long limitedPrivilegesGroupId;
    private long authedUserGroupUserId;
    private long otherUserGroupUserId;
    private long authedUserTagId;
    private long otherUserTagId;
    private long authedUserGroupTagId;
    private long authedUserBookmarkId;
    private long authedUserGroupBookmarkId;
    private long authedUserLimitedGroupBookmarkId;
    private long otherUserBookmarkId;
    private long otherUserLimitedGroupBookmarkId;


    @Autowired
    private BookmarkRepository bookmarkRepo;
    @Autowired
    private TagRepository tagRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private BookmarkTaggingRepository taggingRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private GroupUserRepository groupUserRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @LocalServerPort
    int localPort;
    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    private TestRestTemplate restTemplate;
    private GreenMail greenMail;

    @BeforeAll
    public void initialize() {
        RestTemplateBuilder customTemplateBuilder = restTemplateBuilder.rootUri("http://localhost:" + localPort);
        this.restTemplate = new TestRestTemplate(
                customTemplateBuilder,
                null,
                null,
                TestRestTemplate.HttpClientOption.ENABLE_COOKIES
        );

        this.greenMail = new GreenMail();
        this.greenMail.setUser("test@localhost", "test", "test");
        this.greenMail.start();
    }

    @AfterAll
    public void tearDown() {
        this.greenMail.stop();
    }

    @BeforeEach
    public void setUpDatabase() {
        ArrayList<User> users = new ArrayList<>();

        User authedUser = new User();
        authedUser.setEmail("authedUser@test.com");
        authedUser.setUsername("authedUser");
        authedUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        authedUser.setVerified(true);
        users.add(authedUser);

        User otherUser = new User();
        otherUser.setEmail("otherUser@test.com");
        otherUser.setUsername("otherUser");
        otherUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        otherUser.setVerified(true);
        users.add(otherUser);

        Date tomorrow = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(tomorrow);
        c.add(Calendar.DATE, 1);
        tomorrow = c.getTime();

        User activeUnverifiedUser = new User();
        activeUnverifiedUser.setEmail("activeUnverifiedUser@test.com");
        activeUnverifiedUser.setUsername("activeUnverifiedUser");
        activeUnverifiedUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        activeUnverifiedUser.setVerified(false);
        activeUnverifiedUser.setVerificationCode("abc");
        activeUnverifiedUser.setVerificationExpiryDate(tomorrow);
        users.add(activeUnverifiedUser);

        User expiredUnverifiedUser = new User();
        expiredUnverifiedUser.setEmail("expiredUnverifiedUser@test.com");
        expiredUnverifiedUser.setUsername("expiredUnverifiedUser");
        expiredUnverifiedUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        expiredUnverifiedUser.setVerified(false);
        expiredUnverifiedUser.setVerificationExpiryDate(new Date(0));
        users.add(expiredUnverifiedUser);

        User activeResetUser = new User();
        activeResetUser.setEmail("activeResetUser@test.com");
        activeResetUser.setUsername("activeResetUser");
        activeResetUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        activeResetUser.setVerified(true);
        activeResetUser.setResetPasswordCode("abc");
        activeResetUser.setResetPasswordExpiryDate(tomorrow);
        users.add(activeResetUser);

        User expiredResetUser = new User();
        expiredResetUser.setEmail("expiredResetUser@test.com");
        expiredResetUser.setUsername("expiredResetUser");
        expiredResetUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        expiredResetUser.setVerified(true);
        expiredResetUser.setResetPasswordCode("abc");
        expiredResetUser.setResetPasswordExpiryDate(new Date(0));
        users.add(expiredResetUser);

        userRepo.saveAll(users);

        authedUserId = authedUser.getUserId();
        otherUserId = otherUser.getUserId();
        dummyUserId = expiredUnverifiedUser.getUserId();
    }

    @AfterEach
    public void tearDownDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "\"user\"");
    }

    private HttpHeaders getAuthedHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        HttpEntity<LinkedMultiValueMap<String, String>> req = new HttpEntity<>(map, headers);

        List<String> cookies = restTemplate.postForEntity("/perform_login",
                req, String.class).getHeaders().get("Set-Cookie");

        HttpHeaders authedHeaders = new HttpHeaders();
        authedHeaders.put(HttpHeaders.COOKIE, cookies);
        return authedHeaders;
    }

    @Test
    public void newUser_existingEmail_returns403() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "authedUser@test.com");
        newGroup.put("username", "newUser");
        newGroup.put("password", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void newUser_existingUsername_returns403() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "newUser@test.com");
        newGroup.put("username", "authedUser");
        newGroup.put("password", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void newUser_passwordTooShort_returns403() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "newUser@test.com");
        newGroup.put("username", "newUser");
        newGroup.put("password", "a");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void newUser_replaceExpiredUnverifiedUser_returns200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "expiredUnverifiedUser@test.com");
        newGroup.put("username", "expiredUnverifiedUser");
        newGroup.put("password", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void newUser_returns200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "newUser@test.com");
        newGroup.put("username", "newUser");
        newGroup.put("password", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void resetPassword_nonExistingUser_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "authedUser@test.com");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void resetPassword_resetRequestStillActive_return403() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "activeResetUser@test.com");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void resetPassword_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "authedUser@test.com");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updatePassword_userDoesNotExist_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "invalid@test.com");
        newGroup.put("resetPasswordCode", "abc");
        newGroup.put("newPassword", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updatePassword_wrongResetCode_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "activeResetUser@test.com");
        newGroup.put("resetPasswordCode", "def");
        newGroup.put("newPassword", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updatePassword_expiredResetCode_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "expiredResetUser@test.com");
        newGroup.put("resetPasswordCode", "abc");
        newGroup.put("newPassword", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updatePassword_newPasswordTooShort_return400() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "activeResetUser@test.com");
        newGroup.put("resetPasswordCode", "abc");
        newGroup.put("newPassword", "a");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void updatePassword_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "activeResetUser@test.com");
        newGroup.put("resetPasswordCode", "abc");
        newGroup.put("newPassword", "abcdefghijklmnopqrstuvwxyz");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/reset_password",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteUser_return200() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("password", "pw");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/deleteme",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void verifyUser_userNotFound_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "invalid@test.com");
        newGroup.put("verifyCode", "abc");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/verify",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void verifyUser_userAlreadyVerified_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "authedUser@test.com");
        newGroup.put("verifyCode", "abc");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/verify",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void verifyUser_userVerifyCodeExpired_return403() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "expiredUnverifiedUser@test.com");
        newGroup.put("verifyCode", "abc");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/verify",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void verifyUser_wrongCode_return403() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "activeUnverifiedUser@test.com");
        newGroup.put("verifyCode", "def");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/verify",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void verifyUser_return200() throws Exception {
        HttpHeaders basicHeaders = new HttpHeaders();
        basicHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newGroup = new JSONObject();
        newGroup.put("email", "activeUnverifiedUser@test.com");
        newGroup.put("verifyCode", "abc");
        HttpEntity<String> req = new HttpEntity<>(newGroup.toString(), basicHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/users/verify",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
