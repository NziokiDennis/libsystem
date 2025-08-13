package com.example.library_system.service;

import com.example.library_system.model.User;
import com.example.library_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService Implementation
 * 
 * Integrates Spring Security with our custom User entity and database.
 * Loads user-specific data for authentication and authorization.
 * 
 * Security Features:
 * - Maps database users to Spring Security UserDetails
 * - Converts user roles to Spring Security authorities
 * - Handles account status (active/inactive users)
 * - Provides user information for session management
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Load user by username for Spring Security authentication
     * 
     * @param username The username to look up
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user is not found or inactive
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Attempt to find user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User not found with username: %s", username)
                ));
        
        // Check if user account is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                String.format("User account is disabled: %s", username)
            );
        }
        
        // Create and return Spring Security UserDetails
        return new CustomUserPrincipal(user);
    }
    
    /**
     * Custom UserDetails implementation that wraps our User entity
     * 
     * Provides Spring Security with user authentication and authorization data
     * while maintaining access to our custom User entity properties.
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        /**
         * Get the underlying User entity
         * Useful for accessing custom user properties in controllers
         */
        public User getUser() {
            return user;
        }
        
        /**
         * Map user role to Spring Security authorities
         * Converts User.Role enum to GrantedAuthority with ROLE_ prefix
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            String roleName = "ROLE_" + user.getRole().name();
            return Collections.singletonList(new SimpleGrantedAuthority(roleName));
        }
        
        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }
        
        @Override
        public String getUsername() {
            return user.getUsername();
        }
        
        // Account status methods - all return true for active users
        // These could be enhanced to support more granular account management
        
        @Override
        public boolean isAccountNonExpired() {
            return user.isActive(); // Could implement expiration logic
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return user.isActive(); // Could implement account locking
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return user.isActive(); // Could implement password expiration
        }
        
        @Override
        public boolean isEnabled() {
            return user.isActive();
        }
        
        // Utility methods for easier debugging and logging
        
        @Override
        public String toString() {
            return String.format("CustomUserPrincipal{username='%s', role='%s', active=%s}", 
                               user.getUsername(), user.getRole(), user.isActive());
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            CustomUserPrincipal that = (CustomUserPrincipal) obj;
            return user.getUserId().equals(that.user.getUserId());
        }
        
        @Override
        public int hashCode() {
            return user.getUserId().hashCode();
        }
    }
}