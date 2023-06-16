package org.itmdt.bookmarks.group;

import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public class GroupSpecifications {
    public static Specification<Group> userIsMember(long userId) {
        return new Specification<Group>() {
            @Override
            public Predicate toPredicate(Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Group, GroupUser> groupUser = root.join("groupUsers");
                Join<GroupUser, User> user = groupUser.join("user");
                return cb.equal(user.get("userId"), userId);
            }
        };
    }
    public static Specification<Group> userCanAddBookmarks(long userId) {
        return new Specification<Group>() {
            @Override
            public Predicate toPredicate(Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Group, GroupUser> groupUser = root.join("groupUsers");
                Join<GroupUser, User> user = groupUser.join("user");
                return cb.and(
                        cb.equal(user.get("userId"), userId),
                        cb.equal(groupUser.get("canAddBookmarks"), true)
                );
            }
        };
    }
}
