package org.itmdt.bookmarks.bookmark;

import org.itmdt.bookmarks.bookmarktagging.BookmarkTagging;
import org.itmdt.bookmarks.tag.Tag;
import org.itmdt.bookmarks.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;


public class BookmarkSpecifications {
    public static Specification<Bookmark> belongsToUser(long userId) {
        return new Specification<Bookmark>() {
            @Override
            public Predicate toPredicate(Root<Bookmark> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Bookmark, User> creator = root.join("creator");
                return cb.equal(creator.get("userId"), userId);
            }
        };
    }

    public static Specification<Bookmark> belongsToGroup(long groupId) {
        return new Specification<Bookmark>() {
            @Override
            public Predicate toPredicate(Root<Bookmark> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Bookmark, User> group = root.join("group");
                return cb.equal(group.get("groupId"), groupId);
            }
        };
    }

    public static Specification<Bookmark> doesNotBelongToGroup() {
        return new Specification<Bookmark>() {
            @Override
            public Predicate toPredicate(Root<Bookmark> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Bookmark, User> group = root.join("group", JoinType.LEFT);

                return group.isNull();
            }
        };
    }

    public static Specification<Bookmark> hasTags(List<Long> tagIds) {
        return new Specification<Bookmark>() {
            @Override
            public Predicate toPredicate(Root<Bookmark> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // get a list of all bookmarks with specified tags, then return a predicate that filters out bookmarks
                // not in that list

                Subquery<Bookmark> subquery = query.subquery(Bookmark.class);
                Root<Bookmark> taggingRoot = subquery.from(Bookmark.class);
                Join<Bookmark, BookmarkTagging> subqueryTaggings = taggingRoot.join("taggings");
                Join<BookmarkTagging, Tag> subqueryTaggingsTags = subqueryTaggings.join("tag");

                ArrayList<Predicate> idPredicates = new ArrayList<>();
                for (Long tagId : tagIds) {
                    idPredicates.add(cb.equal(subqueryTaggingsTags.get("tagId"), tagId));
                }

                subquery.select(taggingRoot).where(cb.or(idPredicates.toArray(new Predicate[0])));

                return cb.in(root).value(subquery);
            }
        };
    }

    // This is only for use with in-memory DB. Prod should use an FTS column
    public static Specification<Bookmark> containsPhrase(String phrase) {
        return new Specification<Bookmark>() {
            @Override
            public Predicate toPredicate(Root<Bookmark> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                String wildcardPhrase = "%" + phrase.toLowerCase() + "%";
                return cb.like(
                    cb.lower(
                        cb.concat(
                            root.get(Bookmark_.url),
                            root.get(Bookmark_.displayTitle)
                        )
                    ), wildcardPhrase);
            }
        };
    }
}
