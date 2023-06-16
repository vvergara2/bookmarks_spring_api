package org.itmdt.bookmarks.user;

import com.fasterxml.jackson.annotation.JsonView;
import org.itmdt.bookmarks.BookmarksViews;
import org.itmdt.bookmarks.bookmark.Bookmark;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.itmdt.bookmarks.tag.Tag;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "[user]")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue
    @JsonView(BookmarksViews.Public.class)
    private Long userId;

    @JsonView(BookmarksViews.Public.class)
    private String username;
    @JsonView(BookmarksViews.Private.class)
    private String email;
    @JsonView(BookmarksViews.Private.class)
    private String password;
    @JsonView(BookmarksViews.Public.class)
    private Date createdDate;

    @JsonView(BookmarksViews.Private.class)
    private boolean isVerified;
    @JsonView(BookmarksViews.Private.class)
    private String verificationCode;
    @JsonView(BookmarksViews.Private.class)
    private Date verificationExpiryDate;

    @JsonView(BookmarksViews.Private.class)
    private String resetPasswordCode;
    @JsonView(BookmarksViews.Private.class)
    private Date resetPasswordExpiryDate;

    @JsonView(UserView.FromUserOnly.class)
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Bookmark> bookmarks;
    @JsonView(UserView.FromUserOnly.class)
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Tag> tags;

    @JsonView(UserView.FromUserOnly.class)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<GroupUser> groupUsers;

    public User() {}

    @PrePersist
    void preInsert() {
        if (this.createdDate == null) {
            this.createdDate = new Date();
        }

        if (this.verificationExpiryDate == null) {
            this.verificationExpiryDate = new Date(0);
        }

        if (this.resetPasswordExpiryDate == null) {
            this.resetPasswordExpiryDate = new Date(0);
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public Date getVerificationExpiryDate() {
        return verificationExpiryDate;
    }

    public void setVerificationExpiryDate(Date verificationExpiryDate) {
        this.verificationExpiryDate = verificationExpiryDate;
    }

    public String getResetPasswordCode() {
        return resetPasswordCode;
    }

    public void setResetPasswordCode(String resetPasswordCode) {
        this.resetPasswordCode = resetPasswordCode;
    }

    public Date getResetPasswordExpiryDate() {
        return resetPasswordExpiryDate;
    }

    public void setResetPasswordExpiryDate(Date resetPasswordExpiryDate) {
        this.resetPasswordExpiryDate = resetPasswordExpiryDate;
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Set<GroupUser> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(Set<GroupUser> groupUsers) {
        this.groupUsers = groupUsers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.userId, this.username, this.password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof User)) {
            return false;
        }

        User user = (User) obj;
        return Objects.equals(this.userId, user.userId)
                && Objects.equals(this.username, user.username)
                && Objects.equals(this.password, user.password);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
