package org.itmdt.bookmarks.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE username = :username")
    public User getUserByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE lower(email) = lower(:email)")
    public User getUserByEmail(@Param("email") String email);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "groupUsers",
                    "groupUsers.group"
            }
    )
    @Query("SELECT u FROM User u WHERE userId = :userId")
    public User getUserWithGroupInfo(@Param("userId") Long userId);

}
