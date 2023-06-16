package org.itmdt.bookmarks.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String submittedUsername) throws UsernameNotFoundException {
        // Allow logging in with either username or email
        User user = userRepository.getUserByUsername(submittedUsername);

        if (user == null) {
            user = userRepository.getUserByEmail(submittedUsername);
        }

        if (user == null) {
            throw new UsernameNotFoundException("Could not find user with username or email " + submittedUsername);
        }

        return new UserDetails(user);
    }
}
