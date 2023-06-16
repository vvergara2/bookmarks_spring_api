package org.itmdt.bookmarks;

import net.minidev.json.JSONArray;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BookmarksApplication.class, webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class GroupUserControllerIntegrationTest {
    private long authedUserId;
    private long otherUserId;
    private long dummyUserId;
    private long noGroupsUserId;
    private long authedUserGroupId;
    private long otherUserGroupId;
    private long dummyGroupId;
    private long limitedPrivilegesGroupId;
    private long successorFallbackGroupId;
    private long noPrivilegesGroupId;
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

    @PostConstruct
    public void initialize() {
        RestTemplateBuilder customTemplateBuilder = restTemplateBuilder.rootUri("http://localhost:" + localPort);
        this.restTemplate = new TestRestTemplate(
                customTemplateBuilder,
                null,
                null,
                TestRestTemplate.HttpClientOption.ENABLE_COOKIES
        );

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

        User dummyUser = new User();
        dummyUser.setEmail("dummyUser@test.com");
        dummyUser.setUsername("dummyUser");
        dummyUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        dummyUser.setVerified(true);
        users.add(dummyUser);

        User noGroupsUser = new User();
        noGroupsUser.setEmail("noGroupsUser@test.com");
        noGroupsUser.setUsername("noGroupsUser");
        noGroupsUser.setPassword("$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu");
        noGroupsUser.setVerified(true);
        users.add(noGroupsUser);

        userRepo.saveAll(users);

        authedUserId = authedUser.getUserId();
        otherUserId = otherUser.getUserId();
        dummyUserId = dummyUser.getUserId();
        noGroupsUserId = noGroupsUser.getUserId();

        ArrayList<Group> groups = new ArrayList<>();

        Group authedUserGroup = new Group();
        authedUserGroup.setCreator(authedUser);
        authedUserGroup.setOwner(authedUser);
        authedUserGroup.setName("Test Group");
        groups.add(authedUserGroup);

        Group otherUserGroup = new Group();
        otherUserGroup.setCreator(otherUser);
        otherUserGroup.setOwner(otherUser);
        otherUserGroup.setName("Other User Group");
        groups.add(otherUserGroup);

        Group limitedPrivilegesGroup = new Group();
        limitedPrivilegesGroup.setCreator(dummyUser);
        limitedPrivilegesGroup.setOwner(dummyUser);
        limitedPrivilegesGroup.setName("Limited Privileges Group");
        groups.add(limitedPrivilegesGroup);

        Group dummyGroup = new Group();
        dummyGroup.setCreator(dummyUser);
        dummyGroup.setOwner(dummyUser);
        dummyGroup.setName("Dummy Group");
        groups.add(dummyGroup);

        Group successorFallbackGroup = new Group();
        successorFallbackGroup.setCreator(dummyUser);
        successorFallbackGroup.setOwner(dummyUser);
        successorFallbackGroup.setName("Successor Fallback Group");
        groups.add(successorFallbackGroup);

        Group noPrivilegesGroup = new Group();
        noPrivilegesGroup.setCreator(dummyUser);
        noPrivilegesGroup.setOwner(dummyUser);
        noPrivilegesGroup.setName("No Privileges Group");
        groups.add(noPrivilegesGroup);

        groupRepo.saveAll(groups);
        authedUserGroupId = authedUserGroup.getGroupId();
        otherUserGroupId = otherUserGroup.getGroupId();
        limitedPrivilegesGroupId = limitedPrivilegesGroup.getGroupId();
        dummyGroupId = dummyGroup.getGroupId();
        successorFallbackGroupId = successorFallbackGroup.getGroupId();
        noPrivilegesGroupId = noPrivilegesGroup.getGroupId();

        ArrayList<GroupUser> groupUsers = new ArrayList<>();

        GroupUser authedUserGroupUser = new GroupUser(authedUserGroup, authedUser);
        authedUserGroupUser.setAllOwnerPermissions();
        authedUserGroupUser.setPending(false);
        groupUsers.add(authedUserGroupUser);

        GroupUser authedUserOtherGroupInvite = new GroupUser(otherUserGroup, authedUser);
        authedUserOtherGroupInvite.setPending(true);
        groupUsers.add(authedUserOtherGroupInvite);

        // authedUser can add and remove bookmarks in limitedPrivilegesGroup
        GroupUser authedUserLimitedGroupUser = new GroupUser(limitedPrivilegesGroup, authedUser);
        authedUserLimitedGroupUser.setPending(false);
        authedUserLimitedGroupUser.setCanRemoveBookmarks(true);
        authedUserLimitedGroupUser.setCanAddBookmarks(true);
        groupUsers.add(authedUserLimitedGroupUser);

        GroupUser otherUserGroupUser = new GroupUser(otherUserGroup, otherUser);
        otherUserGroupUser.setAllOwnerPermissions();
        otherUserGroupUser.setPending(false);
        groupUsers.add(otherUserGroupUser);

        // can add but not remove bookmarks in limitedPrivilegesGroup
        // so they can delete their own bookmarks but not somebody else's
        GroupUser otherUserLimitedGroupUser = new GroupUser(limitedPrivilegesGroup, otherUser);
        otherUserLimitedGroupUser.setPending(false);
        otherUserLimitedGroupUser.setCanAddBookmarks(true);
        otherUserLimitedGroupUser.setCanRemoveUsers(true);
        groupUsers.add(otherUserLimitedGroupUser);

        GroupUser dummyUserLimitedGroupUser = new GroupUser(limitedPrivilegesGroup, dummyUser);
        dummyUserLimitedGroupUser.setPending(false);
        dummyUserLimitedGroupUser.setAllOwnerPermissions();
        groupUsers.add(dummyUserLimitedGroupUser);

        GroupUser noGroupsUserLimitedGroupUser = new GroupUser(limitedPrivilegesGroup, noGroupsUser);
        noGroupsUserLimitedGroupUser.setPending(true);
        groupUsers.add(noGroupsUserLimitedGroupUser);

        GroupUser dummyGroupDummyGroupUser = new GroupUser(dummyGroup, dummyUser);
        dummyGroupDummyGroupUser.setPending(false);
        dummyGroupDummyGroupUser.setAllOwnerPermissions();
        groupUsers.add(dummyGroupDummyGroupUser);

        GroupUser fallbackGroupDummyGroupUser = new GroupUser(successorFallbackGroup, dummyUser);
        fallbackGroupDummyGroupUser.setPending(false);
        fallbackGroupDummyGroupUser.setAllOwnerPermissions();
        groupUsers.add(fallbackGroupDummyGroupUser);

        GroupUser fallbackGroupAuthedGroupUser = new GroupUser(successorFallbackGroup, authedUser);
        fallbackGroupAuthedGroupUser.setPending(false);
        groupUsers.add(fallbackGroupAuthedGroupUser);

        GroupUser fallbackGroupOtherGroupUser = new GroupUser(successorFallbackGroup, authedUser);
        fallbackGroupAuthedGroupUser.setPending(false);
        fallbackGroupOtherGroupUser.setJoinDate(new Date(0));
        groupUsers.add(fallbackGroupAuthedGroupUser);

        GroupUser noPrivilegesGroupDummyGroupUser = new GroupUser(noPrivilegesGroup, dummyUser);
        noPrivilegesGroupDummyGroupUser.setPending(false);
        noPrivilegesGroupDummyGroupUser.setAllOwnerPermissions();
        groupUsers.add(noPrivilegesGroupDummyGroupUser);

        GroupUser noPrivilegesGroupAuthedGroupUser = new GroupUser(noPrivilegesGroup, authedUser);
        noPrivilegesGroupAuthedGroupUser.setPending(false);
        groupUsers.add(noPrivilegesGroupAuthedGroupUser);

        GroupUser noPrivilegesGroupOtherGroupUser = new GroupUser(noPrivilegesGroup, otherUser);
        noPrivilegesGroupOtherGroupUser.setPending(false);
        groupUsers.add(noPrivilegesGroupOtherGroupUser);

        groupUserRepo.saveAll(groupUsers);
    }

    @AfterEach
    public void tearDownDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,  "group_user", "\"group\"", "\"user\"");
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
    public void createInvite_noInvitePerms_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/invite?groupId="
                                + limitedPrivilegesGroupId
                                + "&recipientId="
                                + noGroupsUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void createInvite_alreadyMember_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/invite?groupId="
                                + authedUserGroupId
                                + "&recipientId="
                                + authedUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/invite?groupId="
                                + authedUserGroupId
                                + "&recipientId="
                                + otherUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void rescindInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/rescindinvite?groupId="
                                + otherUserGroupId
                                + "&recipientId="
                                + authedUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void rescindInvite_noPermission_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/rescindinvite?groupId="
                                + limitedPrivilegesGroupId
                                + "&recipientId="
                                + noGroupsUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void rescindInvite_alreadyAccepted_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/rescindinvite?groupId="
                                + limitedPrivilegesGroupId
                                + "&recipientId="
                                + authedUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void acceptInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/acceptinvite?groupId="
                                + otherUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void acceptInvite_noInvite_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/acceptinvite?groupId="
                                + otherUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        // no invite exists so should return 404 when retrieving group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void acceptInvite_alreadyAcceptedInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/acceptinvite?groupId="
                                + authedUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void denyInvite_noPendingInvite_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/denyinvite?groupId="
                                + authedUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        // no invite exists so should return 404 when retrieving group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void denyInvite_alreadyAcceptedInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/denyinvite?groupId="
                                + authedUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void denyInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/denyinvite?groupId="
                                + otherUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void ignoreInvite_noPendingInvite_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/ignoreinvite?groupId="
                                + authedUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        // no invite exists so should return 404 when looking for pending group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void ignoreInvite_alreadyAcceptedInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/ignoreinvite?groupId="
                                + authedUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void ignoreInvite_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/ignoreinvite?groupId="
                                + otherUserGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getGroupUsersForLoggedInUser_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/me",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getGroupUsersForGroup_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/group/" + authedUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getGroupUsersForGroup_notMember_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/group/" + otherUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        // authedUser not member of otherUserGroup so should return 404 when retrieving group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getGroupUsersForGroup_pendingFilterButNoInvitePrivileges_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/group/" + limitedPrivilegesGroupId + "?pending=true",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void removeUser_notMember_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/removeuser?groupId="
                                + otherUserGroupId
                                + "&removeUserId="
                                + otherUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        // dummyUser not member of otherUserGroup so should return 404 when retrieving group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void removeUser_noPrivilege_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/removeuser?groupId="
                                + limitedPrivilegesGroupId
                                + "&removeUserId="
                                + otherUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void removeUser_recipientNotMember_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/removeuser?groupId="
                                + authedUserGroupId
                                + "&removeUserId="
                                + noGroupsUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        // recipient not member so should return 404 when retrieving group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void removeUser_tryToRemoveOwner_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/removeuser?groupId="
                                + limitedPrivilegesGroupId
                                + "&removeUserId="
                                + dummyUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void removeUser_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/removeuser?groupId="
                                + limitedPrivilegesGroupId
                                + "&removeUserId="
                                + authedUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void leaveGroup_return200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/leavegroup?groupId="
                                + limitedPrivilegesGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void leaveGroup_notMember_return403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/leavegroup?groupId="
                                + dummyGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void leaveGroup_ownerWithSpecifiedSuccessor_successorGetsPrivileges() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/leavegroup?groupId="
                                +  limitedPrivilegesGroupId
                                + "&successorId="
                                + otherUserId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // check if otherUser now has privileges
        HttpEntity<Void> authedEntity2 = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));
        ResponseEntity<String> response2 =
                restTemplate.exchange(
                        "/groupusers/me",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response2.getBody().contains("canGrantInviteUsersPermission\":true"));
    }

    @Test
    public void leaveGroup_ownerButNoSpecifiedSuccessor_successorGetsPrivileges() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/leavegroup?groupId="
                                +  limitedPrivilegesGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // check if authedUser now has privileges
        HttpEntity<Void> authedEntity2 = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response2 =
                restTemplate.exchange(
                        "/groupusers/me",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response2.getBody().contains("canGrantInviteUsersPermission\":true"));
    }

    @Test
    public void leaveGroup_triggerFallbackSuccessor_successorGetsPrivileges() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/leavegroup?groupId="
                                +  successorFallbackGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // check if authedUser now has privileges
        HttpEntity<Void> authedEntity2 = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));
        ResponseEntity<String> response2 =
                restTemplate.exchange(
                        "/groupusers/me",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response2.getBody().contains("canGrantInviteUsersPermission\":true"));
    }

    @Test
    public void leaveGroup_lastMember_deletesGroup() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("dummyUser", "pw"));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/leavegroup?groupId="
                                +  dummyGroupId,
                        HttpMethod.POST,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Group hopefullyNull = groupRepo.findById(dummyGroupId).orElse(null);
        assertEquals(null, hopefullyNull);
    }

    @Test
    public void updateGroupUser_notMember_returns403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", dummyUserId);
        modifiedGroupUser.put("groupId", dummyGroupId);
        modifiedGroupUser.put("canGrantAddBookmarksPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_targetNotMember_returns404() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", dummyUserId);
        modifiedGroupUser.put("groupId", authedUserGroupId);
        modifiedGroupUser.put("canGrantAddBookmarksPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changeOwnPermissions_silentFailWith200() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", limitedPrivilegesGroupId);
        modifiedGroupUser.put("canGrantAddBookmarksPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canAddBookmarks_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canAddBookmarks", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canRemoveBookmarks_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canRemoveBookmarks", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canInviteUsers_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canInviteUsers", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canRemoveUsers_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canRemoveUsers", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canGrantAddBookmarksPermission_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canGrantAddBookmarksPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canGrantRemoveBookmarksPermission_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canGrantRemoveBookmarksPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canGrantInviteUsersPermission_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canGrantInviteUsersPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_changePermissionNoPriv_canGrantRemoveUsersPermission_return403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", authedUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canGrantRemoveUsersPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateGroupUser_hasPrivileges_return200() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("dummyUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject bulkUpdateObject = new JSONObject();
        JSONArray groupUserArray = new JSONArray();

        // assemble post json
        JSONObject modifiedGroupUser = new JSONObject();
        modifiedGroupUser.put("userId", otherUserId);
        modifiedGroupUser.put("groupId", noPrivilegesGroupId);
        modifiedGroupUser.put("canAddBookmarks", true);
        modifiedGroupUser.put("canRemoveBookmarks", true);
        modifiedGroupUser.put("canInviteUsers", true);
        modifiedGroupUser.put("canRemoveUsers", true);
        modifiedGroupUser.put("canGrantAddBookmarksPermission", true);
        modifiedGroupUser.put("canGrantRemoveBookmarksPermission", true);
        modifiedGroupUser.put("canGrantInviteUsersPermission", true);
        modifiedGroupUser.put("canGrantRemoveUsersPermission", true);
        groupUserArray.add(modifiedGroupUser);

        bulkUpdateObject.put("groupUserUpdateList", groupUserArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(bulkUpdateObject.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/groupusers/bulkupdate",
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
