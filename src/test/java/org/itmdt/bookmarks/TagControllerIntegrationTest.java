package org.itmdt.bookmarks;

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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.http.*;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.PostConstruct;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BookmarksApplication.class, webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TagControllerIntegrationTest {
    private long authedUserId;
    private long otherUserId;
    private long dummyUserId;
    private long authedUserGroupId;
    private long otherUserGroupId;
    private long limitedPrivilegesGroupId;
    private long authedUserGroupUserId;
    private long otherUserGroupUserId;
    private long authedUserTagId;
    private long authedUserLimitedTagId;
    private long otherUserTagId;
    private long otherUserLimitedTagId;
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

        userRepo.saveAll(users);

        authedUserId = authedUser.getUserId();
        otherUserId = otherUser.getUserId();
        dummyUserId = dummyUser.getUserId();

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

        groupRepo.saveAll(groups);
        authedUserGroupId = authedUserGroup.getGroupId();
        otherUserGroupId = otherUserGroup.getGroupId();
        limitedPrivilegesGroupId = limitedPrivilegesGroup.getGroupId();

        ArrayList<GroupUser> groupUsers = new ArrayList<>();

        GroupUser authedUserGroupUser = new GroupUser(authedUserGroup, authedUser);
        authedUserGroupUser.setAllOwnerPermissions();
        authedUserGroupUser.setPending(false);
        groupUsers.add(authedUserGroupUser);

        // can add and remove bookmarks in limitedPrivilegesGroup
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
        groupUsers.add(otherUserLimitedGroupUser);

        groupUserRepo.saveAll(groupUsers);

        ArrayList<Tag> tags = new ArrayList<>();

        Tag authedUserTag = new Tag("authedUserTag", authedUser);
        tags.add(authedUserTag);

        Tag authedUserExtraTag = new Tag("authedUserExtraTag", authedUser);
        tags.add(authedUserExtraTag);

        Tag authedUserLimitedTag = new Tag("authedUserLimitedTag", authedUser);
        authedUserLimitedTag.setGroup(limitedPrivilegesGroup);
        tags.add(authedUserLimitedTag);

        Tag otherUserTag = new Tag("otherUserTag", otherUser);
        tags.add(otherUserTag);

        Tag otherUserLimitedTag = new Tag("otherUserLimitedTag", otherUser);
        otherUserLimitedTag.setGroup(limitedPrivilegesGroup);
        tags.add(otherUserLimitedTag);

        Tag authedUserGroupTag = new Tag("authedUserGroupTag", authedUser);
        authedUserGroupTag.setGroup(authedUserGroup);
        tags.add(authedUserGroupTag);

        tagRepo.saveAll(tags);

        otherUserLimitedTagId = otherUserLimitedTag.getTagId();
        authedUserLimitedTagId = authedUserLimitedTag.getTagId();
        authedUserTagId = authedUserTag.getTagId();
        otherUserTagId = otherUserTag.getTagId();
        authedUserGroupTagId = authedUserGroupTag.getTagId();

        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Bookmark authedUserBookmark = new Bookmark(authedUser, "authedUserBookmark");
        bookmarks.add(authedUserBookmark);

        Bookmark authedUserNoTagBookmark = new Bookmark(authedUser, "authedUserNoTagBookmark");
        bookmarks.add(authedUserNoTagBookmark);

        Bookmark authedUserGroupBookmark = new Bookmark(authedUser, "authedUserGroupBookmark");
        authedUserGroupBookmark.setGroup(authedUserGroup);
        bookmarks.add(authedUserGroupBookmark);

        Bookmark authedUserLimitedGroupBookmark = new Bookmark(authedUser, "authedUserLimitedGroupBookmark");
        authedUserLimitedGroupBookmark.setGroup(limitedPrivilegesGroup);
        bookmarks.add(authedUserLimitedGroupBookmark);

        Bookmark otherUserBookmark = new Bookmark(otherUser, "otherUserBookmark");
        bookmarks.add(otherUserBookmark);

        Bookmark otherUserLimitedGroupBookmark = new Bookmark(otherUser, "otherUserLimitedGroupBookmark");
        otherUserLimitedGroupBookmark.setGroup(limitedPrivilegesGroup);
        bookmarks.add(otherUserLimitedGroupBookmark);

        bookmarkRepo.saveAll(bookmarks);

        authedUserBookmarkId = authedUserBookmark.getBookmarkId();
        authedUserGroupBookmarkId = authedUserGroupBookmark.getBookmarkId();
        authedUserLimitedGroupBookmarkId = authedUserLimitedGroupBookmark.getBookmarkId();
        otherUserBookmarkId = otherUserBookmark.getBookmarkId();
        otherUserLimitedGroupBookmarkId = otherUserLimitedGroupBookmark.getBookmarkId();

        ArrayList<BookmarkTagging> taggings = new ArrayList<>();

        BookmarkTagging authedUserTagOnAuthedBookmark =
                new BookmarkTagging(authedUserBookmark, authedUserTag);
        taggings.add(authedUserTagOnAuthedBookmark);

        BookmarkTagging otherUserTagOnOtherBookmark =
                new BookmarkTagging(otherUserBookmark, otherUserTag);
        taggings.add(otherUserTagOnOtherBookmark);

        BookmarkTagging groupTagOnGroupBookmark =
                new BookmarkTagging(authedUserGroupBookmark, authedUserGroupTag);
        taggings.add(groupTagOnGroupBookmark);

        taggingRepo.saveAll(taggings);
    }

    @AfterEach
    public void tearDownDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,  "bookmark_tagging", "bookmark", "tag", "group_user", "\"group\"", "\"user\"");
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
    public void getTags_returns401IfUnauthorized() throws Exception {
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        String.class
                );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void getTags_noAdditionalParams_returnsOnlyPrivateTags() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("authedUserTag"));
        assertFalse(response.getBody().contains("otherUserTag"));
        assertFalse(response.getBody().contains("authedUserGroupTag"));
    }

    @Test
    public void getTags_withSortAndOrder_returnsCorrectOrder() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags?sort=name&order=asc",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        int firstIndex = response.getBody().indexOf("authedUserExtraTag");
        int secondIndex = response.getBody().indexOf("authedUserTag");
        assertTrue(firstIndex > 0 && secondIndex > 0);
        assertTrue(firstIndex < secondIndex);
    }

    @Test
    public void getTags_badSortAndOrder_returns400() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags?sort=invalid&order=asc",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void getTags_withGroupFilter_returnsOnlyGroupTag() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags?groupId=" + authedUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("authedUserGroupTag"));
        assertFalse(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void getTags_withGroupFilterButNotMember_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags?groupId=" + otherUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        // user is not member of group, so will return 404 for not finding corresponding group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getTags_withGroupAndCreatorFilters_returnsOnlyCorrectTag() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags?groupId=" + limitedPrivilegesGroupId + "&creatorId=" + otherUserId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("otherUserLimitedTag"));
        assertFalse(response.getBody().contains("authedUserLimitedTag"));
    }

    @Test
    public void getTags_withSearchFilter_returnsOnlyCorrectTag() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags?search=authedUserExtraTag",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("authedUserExtraTag"));
        assertFalse(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void updateTag_returnsUpdatedTag() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedTag = new JSONObject();
        updatedTag.put("name", "newTagName");
        HttpEntity<String> req = new HttpEntity<>(updatedTag.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserTagId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertTrue(response.getBody().contains("newTagName"));
        assertFalse(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void updateTag_otherUserTag_returns403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedTag = new JSONObject();
        updatedTag.put("name", "newTagName");
        HttpEntity<String> req = new HttpEntity<>(updatedTag.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + otherUserTagId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateTag_otherUserTagInGroupWithPrivileges_returns200() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedTag = new JSONObject();
        updatedTag.put("name", "newTagName");
        HttpEntity<String> req = new HttpEntity<>(updatedTag.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + otherUserLimitedTagId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertTrue(response.getBody().contains("newTagName"));
    }

    @Test
    public void updateTag_otherUserTagInGroupWithoutPrivileges_returns403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedTag = new JSONObject();
        updatedTag.put("name", "newTagName");
        HttpEntity<String> req = new HttpEntity<>(updatedTag.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserLimitedTagId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        // otherUser does not have remove bookmark permission in group, which is
        // required to modify tags
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateTag_otherUserTagInGroupNotMember_returns403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedTag = new JSONObject();
        updatedTag.put("name", "newTagName");
        HttpEntity<String> req = new HttpEntity<>(updatedTag.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserGroupTagId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        // otherUser is not a member of authedUserGroup so finding group user returns 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getTagById_returnsTag() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserTagId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void getTagById_otherUserPrivateTag_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + otherUserTagId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void getTagById_tagFromGroupAndUserIsMember_returnsTag() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserGroupTagId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("authedUserGroupTag"));
    }

    @Test
    public void getTagById_tagFromGroupAndUserIsNotMember_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserGroupTagId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        // otherUser not member of authedUserGroup, returns 404 when retrieving group user
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteTag_returns200() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserTagId,
                        HttpMethod.DELETE,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteTag_otherUser_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + otherUserTagId,
                        HttpMethod.DELETE,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteTag_otherUserSameGroupWithPrivilege_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + otherUserLimitedTagId,
                        HttpMethod.DELETE,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteTag_otherUserSameGroupButNoPrivilege_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("otherUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/tags/" + authedUserLimitedTagId,
                        HttpMethod.DELETE,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}