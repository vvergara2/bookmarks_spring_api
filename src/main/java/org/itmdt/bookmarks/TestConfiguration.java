/**
 * This is used to create a default user for testing only!
 */

package org.itmdt.bookmarks;

import com.github.javafaker.Faker;
import com.icegreen.greenmail.spring.GreenMailBean;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class TestConfiguration {

    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private GroupUserRepository groupUserRepo;
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private BookmarkTaggingRepository taggingRepository;
    @Autowired
    private UserRepository userRepository;


    // used for testing user verify/reset password emails
    @Bean
    @Profile("development")
    public GreenMailBean greenMail() {
        GreenMailBean greenMailBean = new GreenMailBean();
        String[] emailUsers = {"test:test@test.local"};
        greenMailBean.setUsers(Arrays.asList(emailUsers));
        return greenMailBean;
    }

    private String pickRandomBookmarkName(Faker faker, HashSet<String> usedSet) {
        String pick = null;

        while (pick == null || usedSet.contains(pick)) {
            final int randomInt = faker.random().nextInt(7);
            switch(randomInt) {
                case 0:
                    pick = faker.food().dish();
                    break;
                case 1:
                    pick = faker.book().title();
                    break;
                case 2:
                    pick = faker.artist().name();
                    break;
                case 3:
                    pick = faker.twinPeaks().location();
                    break;
                case 4:
                    pick = faker.dog().breed();
                    break;
                case 5:
                    pick = faker.company().name();
                    break;
                case 6:
                    pick = faker.university().name();
                    break;
            }
        }

        usedSet.add(pick);

        return pick;
    }

    private String pickRandomTagName(Faker faker, HashSet<String> usedSet) {
        String pick = null;

        while (pick == null || usedSet.contains(pick)) {
            final int randomInt = faker.random().nextInt(5);
            switch(randomInt) {
                case 0:
                    pick = faker.color().name();
                    break;
                case 1:
                    pick = faker.animal().name();
                    break;
                case 2:
                    pick = faker.country().countryCode3();
                    break;
                case 3:
                    pick = faker.hacker().noun();
                    break;
                case 4:
                    pick = faker.hipster().word();
                    break;
            }
        }

        usedSet.add(pick);

        return pick;
    }

    @Bean
    @Profile({"development", "demo"})
    CommandLineRunner initDatabase() {
        return args -> {
            // hash of "pw"
            final String PW_HASH = "$2a$12$WDNkHwKLmkRJ1hOmSGZeFuQSbNLlgOhr/ptNhP1EPOieZ2NBLAVNu";
            final String[] USERNAMES = {"fnord", "johndoe", "clarice", "spot"};
            final int SENTENCE_WORD_COUNT = 25;

            // these are per-user
            final int PERSONAL_TAG_COUNT = 5;
            final int GROUP_TAG_COUNT = 3;
            final int BOOKMARKS_PER_TAG = 10;

            // avoid creating multiple tags with the same name
            HashSet<String> usedRandomStrings = new HashSet<>();

            Faker faker = new Faker();

            ArrayList<User> users = new ArrayList<>();
            for (String userName : USERNAMES) {
                User newUser = new User();
                newUser.setEmail(userName + "@domain.test");
                newUser.setUsername(userName);
                newUser.setPassword(PW_HASH);
                newUser.setVerified(true);
                users.add(newUser);
            }
            userRepository.saveAll(users);

            ArrayList<Group> groups = new ArrayList<>();
            for (User user : users) {
                Group newUserGroup = new Group();
                newUserGroup.setCreator(user);
                newUserGroup.setOwner(user);
                newUserGroup.setName(user.getUsername() + "'s Group");

                groups.add(newUserGroup);
            }
            groupRepo.saveAll(groups);

            ArrayList<GroupUser> groupUsers = new ArrayList<>();
            for (Group group : groups) {
                for (User user : users) {
                    GroupUser newGroupUser = new GroupUser(group, user);
                    if (group.getOwner() == user) {
                        newGroupUser.setAllOwnerPermissions();
                    } else {
                        newGroupUser.setCanAddBookmarks(true);
                    }
                    newGroupUser.setPending(false);

                    // add to commit list
                    groupUsers.add(newGroupUser);

                    // also add to user for reference below
                    Set<GroupUser> userGroupUsers = user.getGroupUsers();
                    if (userGroupUsers == null) {
                        userGroupUsers = new HashSet<>();
                    }
                    userGroupUsers.add(newGroupUser);
                    user.setGroupUsers(userGroupUsers);
                }
            }
            groupUserRepo.saveAll(groupUsers);

            ArrayList<Tag> tags = new ArrayList<>();
            for (User user : users) {
                List<Tag> userTags = user.getTags();
                if (userTags == null) {
                    userTags = new ArrayList<>();
                }

                for (int i = 0; i < PERSONAL_TAG_COUNT; i++) {
                    Tag newPersonalTag = new Tag(pickRandomTagName(faker, usedRandomStrings), user);
                    tags.add(newPersonalTag);
                    userTags.add(newPersonalTag);
                }

                for (GroupUser groupUser : user.getGroupUsers()) {
                    if (!groupUser.getCanAddBookmarks()) {
                        continue;
                    }

                    Set<Tag> groupTags = groupUser.getGroup().getTags();
                    if (groupTags == null) {
                        groupTags = new HashSet<>();
                    }

                    for (int i = 0; i < GROUP_TAG_COUNT; i++) {
                        Tag newGroupTag = new Tag(pickRandomTagName(faker, usedRandomStrings), user);
                        newGroupTag.setGroup(groupUser.getGroup());

                        tags.add(newGroupTag);
                        userTags.add(newGroupTag);
                        groupTags.add(newGroupTag);
                    }
                }

                user.setTags(userTags);
            }
            tagRepository.saveAll(tags);

            ArrayList<BookmarkTagging> taggings = new ArrayList<>();
            for (User user : users) {
                for (Tag tag : user.getTags()) {
                    for (int i = 0; i < BOOKMARKS_PER_TAG; i++) {
                        Bookmark newBookmark = new Bookmark(user, "https://" + faker.internet().url());
                        if (tag.getGroup() != null) {
                            newBookmark.setGroup(tag.getGroup());
                        }

                        newBookmark.setDisplayTitle(pickRandomBookmarkName(faker, usedRandomStrings));
                        newBookmark.setDescription(faker.lorem().sentence(SENTENCE_WORD_COUNT));
                        Date createdDate = faker.date().birthday();
                        newBookmark.setCreatedDate(createdDate);
                        newBookmark.setLastUpdatedDate(faker.date().future(14, TimeUnit.DAYS, createdDate));
                        bookmarkRepository.save(newBookmark);

                        BookmarkTagging newTagging = new BookmarkTagging(newBookmark, tag);
                        taggings.add(newTagging);
                    }
                }
            }

            taggingRepository.saveAll(taggings);
        };
    }
}
