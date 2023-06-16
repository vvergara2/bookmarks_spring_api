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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BookmarksApplication.class, webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class BookmarkControllerIntegrationTest {
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

        Tag otherUserTag = new Tag("otherUserTag", otherUser);
        tags.add(otherUserTag);

        Tag authedUserGroupTag = new Tag("authedUserGroupTag", authedUser);
        authedUserGroupTag.setGroup(authedUserGroup);
        tags.add(authedUserGroupTag);

        tagRepo.saveAll(tags);

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
    public void getBookmarks_returns401IfUnauthorized() throws Exception {
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        String.class
                );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void getBookmarks_groupFilterButNotMember_returns403() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?groupId=" + otherUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getBookmarks_groupAndCreatorFilters_returnsMatchOnly() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?groupId=" + limitedPrivilegesGroupId
                                + "&creatorId=" + otherUserId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertFalse(response.getBody().contains("authedUserLimitedGroupBookmark"));
        assertTrue(response.getBody().contains("otherUserLimitedGroupBookmark"));
    }

    @Test
    public void getBookmarks_searchPhrase_returnsMatchOnly() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?search=authedUserNoTagBookmark",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertFalse(response.getBody().contains("authedUserBookmark"));
        assertTrue(response.getBody().contains("authedUserNoTagBookmark"));
    }

    @Test
    public void getBookmarks_sortAndOrder_returnsSortedData() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?sort=url&order=asc",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        int authedUserBookmarkIndex = response.getBody().indexOf("authedUserBookmark");
        int authedUserNoTagBookmarkIndex = response.getBody().indexOf("authedUserNoTagBookmark");
        assertTrue(authedUserBookmarkIndex > 0 && authedUserNoTagBookmarkIndex > 0);
        assertTrue(authedUserBookmarkIndex < authedUserNoTagBookmarkIndex);
    }

    @Test
    public void getBookmarks_badSortAndOrder_returns400() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?sort=invalid&order=asc",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getBookmarks_returnsOnlyAuthed() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("authedUserBookmark"));
        assertTrue(response.getBody().contains("authedUserNoTagBookmark"));

        // if no group specified, only return authed user's bookmarks
        assertFalse(response.getBody().contains("groupBookmark"));

        assertFalse(response.getBody().contains("otherUserBookmark"));

        // should be joined with tag info
        assertTrue(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void getBookmarks_withTagFilter_returnsOnlyAuthed() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?tagIds=" + authedUserTagId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(response.getBody().contains("authedUserBookmark"));
        assertFalse(response.getBody().contains("authedUserNoTagBookmark"));

        // if no group specified, only return authed user's bookmarks
        assertFalse(response.getBody().contains("groupBookmark"));

        assertFalse(response.getBody().contains("otherUserBookmark"));

        // should be joined with tag info
        assertTrue(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void getBookmarks_withBadTagFilter_returns400() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?tagIds=abc",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getBookmarks_groupSpecified_returnsOnlyGroupBookmarks() throws Exception {
        HttpEntity<Void> authedEntity = new HttpEntity<>(getAuthedHeaders("authedUser", "pw"));
        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks?groupId=" + authedUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertFalse(response.getBody().contains("authedUserBookmark"));

        // if no group specified, only return authed user's bookmarks
        assertTrue(response.getBody().contains("authedUserGroupBookmark"));

        assertFalse(response.getBody().contains("otherUserBookmark"));

        // should be joined with tag info
        assertFalse(response.getBody().contains("authedUserTag"));
    }

    @Test
    public void newBookmark_returnsInAllBookmarks() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());

        HttpEntity<Void> authedEntity = new HttpEntity<>(authedHeaders);
        ResponseEntity<String> allResponse =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(allResponse.getBody().contains("newBookmark"));
        assertTrue(allResponse.getBody().contains("authedUserBookmark"));

        // if no group specified, only return authed user's bookmarks
        assertFalse(allResponse.getBody().contains("groupBookmark"));
        assertFalse(allResponse.getBody().contains("otherUserBookmark"));

        // should be joined with tag info
        assertTrue(allResponse.getBody().contains("authedUserTag"));
    }

    @Test
    public void newBookmark_publishToGroup_returnsInAllBookmarks() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("groupId", authedUserGroupId);
        newBookmark.put("url", "newBookmark");
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());

        HttpEntity<Void> authedEntity = new HttpEntity<>(authedHeaders);
        ResponseEntity<String> allResponse =
                restTemplate.exchange(
                        "/bookmarks?groupId=" + authedUserGroupId,
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(allResponse.getBody().contains("newBookmark"));
        assertFalse(allResponse.getBody().contains("authedUserBookmark"));

        // if no group specified, only return authed user's bookmarks
        assertTrue(allResponse.getBody().contains("authedUserGroupBookmark"));
        assertFalse(allResponse.getBody().contains("otherUserBookmark"));

        // should be joined with tag info
        assertFalse(allResponse.getBody().contains("authedUserTag"));
    }

    @Test
    public void newBookmark_publishToOtherGroup_returns403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("groupId", otherUserGroupId);
        newBookmark.put("url", "newBookmark");
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void newBookmark_includeTags_returnsInAllBookmarks() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");

        JSONArray tagArray = new JSONArray();

        JSONObject newTag = new JSONObject();
        newTag.put("tagId", -1);
        newTag.put("name", "newTag");
        tagArray.add(newTag);

        // names for existing tags are ignored
        JSONObject existingTag = new JSONObject();
        existingTag.put("tagId", authedUserTagId);
        existingTag.put("name", "anyName");
        tagArray.add(existingTag);

        JSONObject invalidTag = new JSONObject();
        invalidTag.put("tagId", otherUserTagId);
        invalidTag.put("name", "badTag");
        tagArray.add(invalidTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // should be joined with correct tag info
        assertTrue(response.getBody().contains("newTag"));
        assertTrue(response.getBody().contains("authedUserTag"));
        assertFalse(response.getBody().contains("otherUserTag"));
    }

    @Test
    public void newBookmark_includeBadTag_returnsNoTags() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");

        JSONArray tagArray = new JSONArray();

        // no name left after trimming whitespace, should be rejected
        JSONObject newTag = new JSONObject();
        newTag.put("tagId", -1);
        newTag.put("name", "     ");
        tagArray.add(newTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // resulting bookmark should not have any associated tags
        assertFalse(response.getBody().contains("tagId"));
    }

    @Test
    public void newBookmark_includeNewGroupTag_returnsWithTag() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");
        newBookmark.put("groupId", authedUserGroupId);

        JSONArray tagArray = new JSONArray();

        JSONObject newTag = new JSONObject();
        newTag.put("tagId", -1);
        newTag.put("name", "newGroupTag");
        tagArray.add(newTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // resulting bookmark should have tag
        assertTrue(response.getBody().contains("newGroupTag"));
    }

    @Test
    public void newBookmark_includeExistingGroupTagByName_returnsWithTag() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");
        newBookmark.put("groupId", authedUserGroupId);

        JSONArray tagArray = new JSONArray();

        // written like it's a new tag but a tag already exists with matching name
        JSONObject newTag = new JSONObject();
        newTag.put("tagId", -1);
        newTag.put("name", "authedUserGroupTag");
        tagArray.add(newTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // resulting bookmark should have tag
        assertTrue(response.getBody().contains("authedUserGroupTag"));
    }

    @Test
    public void newBookmark_includeExistingGroupTagFromOtherGroup_returnsWithoutTag() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");
        newBookmark.put("groupId", otherUserGroupId);

        JSONArray tagArray = new JSONArray();

        // this tag belongs to authedUserGroup and should be rejected
        JSONObject newTag = new JSONObject();
        newTag.put("tagId", authedUserGroupTagId);
        newTag.put("name", "whatever");
        tagArray.add(newTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // resulting bookmark should not have any tags
        assertFalse(response.getBody().contains("authedUserGroupTag"));
    }

    @Test
    public void newBookmark_includeExistingGroupTagOnPrivateBookmark_returnsWithoutTag() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");

        JSONArray tagArray = new JSONArray();

        // this tag belongs to authedUserGroup and should be rejected
        JSONObject newTag = new JSONObject();
        newTag.put("tagId", authedUserGroupTagId);
        newTag.put("name", "whatever");
        tagArray.add(newTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // resulting bookmark should not have any tags
        assertFalse(response.getBody().contains("authedUserGroupTag"));
    }

    @Test
    public void newBookmark_includeBogusTag_returnsWithoutTag() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject newBookmark = new JSONObject();
        newBookmark.put("url", "newBookmark");

        JSONArray tagArray = new JSONArray();

        // this tag belongs to authedUserGroup and should be rejected
        JSONObject newTag = new JSONObject();
        newTag.put("tagId", 999999);
        newTag.put("name", "bogusTag");
        tagArray.add(newTag);

        newBookmark.put("desiredTags", tagArray);

        // send post request
        HttpEntity<String> req = new HttpEntity<>(newBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.POST,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("newBookmark"));

        // resulting bookmark should not have any tags
        assertFalse(response.getBody().contains("bogusTag"));
    }

    @Test
    public void updateBookmark_successfullyUpdates() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedBookmark = new JSONObject();
        updatedBookmark.put("url", "updatedBookmark");
        HttpEntity<String> req = new HttpEntity<>(updatedBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + authedUserBookmarkId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());

        HttpEntity<Void> authedEntity = new HttpEntity<>(authedHeaders);
        ResponseEntity<String> allResponse =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(allResponse.getBody().contains("updatedBookmark"));
    }

    @Test
    public void updateBookmark_otherUser_returns403() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject updatedBookmark = new JSONObject();
        updatedBookmark.put("url", "updatedBookmark");
        HttpEntity<String> req = new HttpEntity<>(updatedBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + otherUserBookmarkId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void updateBookmark_differentTags_successfullyUpdates() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);

        // assemble post json
        JSONObject updatedBookmark = new JSONObject();
        updatedBookmark.put("url", "updatedBookmark");

        JSONArray tagArray = new JSONArray();

        // the bookmark we are modifying has a tag that we are purposefully
        // excluding so that it will be removed from the bookmark during update

        JSONObject newTag = new JSONObject();
        newTag.put("tagId", -1);
        newTag.put("name", "newTag");
        tagArray.add(newTag);

        JSONObject invalidTag = new JSONObject();
        invalidTag.put("tagId", otherUserTagId);
        invalidTag.put("name", "badTag");
        tagArray.add(invalidTag);

        updatedBookmark.put("desiredTags", tagArray);

        HttpEntity<String> req = new HttpEntity<>(updatedBookmark.toString(), authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + authedUserBookmarkId,
                        HttpMethod.PUT,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());

        HttpEntity<Void> authedEntity = new HttpEntity<>(authedHeaders);
        ResponseEntity<String> allResponse =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );

        assertTrue(allResponse.getBody().contains("updatedBookmark"));
        assertTrue(allResponse.getBody().contains("newTag"));
        assertFalse(allResponse.getBody().contains("authedUserTag"));
        assertFalse(allResponse.getBody().contains("otherUserTag"));
    }

    @Test
    public void getBookmarkById_otherUser_fails() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + otherUserBookmarkId,
                        HttpMethod.GET,
                        req,
                        String.class
                );

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void getBookmarkById_notMemberOfGroup_fails() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + authedUserGroupBookmarkId,
                        HttpMethod.GET,
                        req,
                        String.class
                );

        assertTrue(response.getBody().contains("group-user-not-found"));
    }

    @Test
    public void getBookmarkById_succeeds() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + authedUserBookmarkId,
                        HttpMethod.GET,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void deleteBookmark_succeeds() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + authedUserBookmarkId,
                        HttpMethod.DELETE,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());

        HttpEntity<Void> authedEntity = new HttpEntity<>(authedHeaders);
        ResponseEntity<String> allResponse =
                restTemplate.exchange(
                        "/bookmarks",
                        HttpMethod.GET,
                        authedEntity,
                        String.class
                );
        assertFalse(allResponse.getBody().contains("authedUserBookmark"));
    }

    @Test
    public void deleteBookmark_otherUser_fails() throws Exception {
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + otherUserBookmarkId,
                        HttpMethod.DELETE,
                        req,
                        String.class
                );

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void deleteBookmark_otherUserButWithPrivilege_succeeds() throws Exception {
        // authedUser does not own the bookmark to be deleted but they do have
        // delete bookmark privileges in the group, so it should succeed
        HttpHeaders authedHeaders = getAuthedHeaders("authedUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + otherUserLimitedGroupBookmarkId,
                        HttpMethod.DELETE,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void deleteBookmark_otherUserAndNoPrivilege_returns403() throws Exception {
        // otherUser has add bookmark privilege but not delete
        // which means they can't delete another member's bookmarks
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + authedUserLimitedGroupBookmarkId,
                        HttpMethod.DELETE,
                        req,
                        String.class
                );

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void deleteBookmark_noPrivilegeButIsOwner_succeeds() throws Exception {
        // otherUser does not have delete bookmark privilege for this group
        // but because they own the bookmark they can still delete it
        HttpHeaders authedHeaders = getAuthedHeaders("otherUser", "pw");
        authedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(authedHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/bookmarks/" + otherUserLimitedGroupBookmarkId,
                        HttpMethod.DELETE,
                        req,
                        String.class
                );

        assertEquals(200, response.getStatusCodeValue());
    }
}
