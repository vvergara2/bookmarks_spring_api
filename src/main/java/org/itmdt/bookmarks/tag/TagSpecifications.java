package org.itmdt.bookmarks.tag;

import org.itmdt.bookmarks.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public class TagSpecifications {
    public static Specification<Tag> belongsToUser(long userId) {
        return new Specification<Tag>() {
            @Override
            public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Tag, User> creator = root.join("creator");
                return cb.equal(creator.get("userId"), userId);
            }
        };
    }
    public static Specification<Tag> belongsToGroup(long groupId) {
        return new Specification<Tag>() {
            @Override
            public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Tag, User> group = root.join("group");

                return cb.equal(group.get("groupId"), groupId);
            }
        };
    }

    public static Specification<Tag> doesNotBelongToGroup() {
        return new Specification<Tag>() {
            @Override
            public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Tag, User> group = root.join("group", JoinType.LEFT);

                return group.isNull();
            }
        };
    }

    // TODO replace with db specific FTS filter
    public static Specification<Tag> containsPhrase(String phrase) {
        return new Specification<Tag>() {
            @Override
            public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                String wildcardPhrase = "%" + phrase.toLowerCase() + "%";
                return cb.like(
                        cb.lower(
                            root.get(Tag_.name)
                        ), wildcardPhrase);
            }
        };
    }
}
