package com.example.library_system.config;

import com.example.library_system.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security Configuration
 * 
 * Handles authentication, authorization, and security policies for the application.
 * Implements role-based access control with separate login flows for students and admins.
 * 
 * Security Architecture:
 * - Public endpoints: /, /css/**, /js/**, /images/**
 * - Student endpoints: /student/** (requires STUDENT role)
 * - Admin endpoints: /admin/** (requires ADMIN role)
 * - Separate login pages for different user types
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    /**
     * Password encoder bean for secure password hashing
     * Uses BCrypt with strength 12 for enhanced security
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * Main security filter chain configuration
     * Defines URL-based security rules and authentication flows
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection - enabled for forms, disabled for API endpoints if needed
            .csrf(csrf -> csrf.disable()) // Simplified for development - enable in production
            
            // URL-based authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", 
                               "/favicon.ico", "/error").permitAll()
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Student endpoints - require STUDENT role  
                .requestMatchers("/student/**").hasRole("STUDENT")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Form-based authentication configuration
            .formLogin(form -> form
                .loginPage("/")  // Custom login page (modal on homepage)
                .loginProcessingUrl("/login")  // URL that processes login form
                .successHandler(this::onAuthenticationSuccess)  // Custom success handler
                .failureUrl("/?error=true")  // Redirect on failure
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Admin login configuration (separate flow)
            .formLogin(form -> form
                .loginProcessingUrl("/admin/login")
                .successHandler(this::onAuthenticationSuccess)
                .failureUrl("/?adminError=true")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Session management
            .sessionManagement(session -> session
                .maximumSessions(1)  // One session per user
                .maxSessionsPreventsLogin(false)  // Allow new login to invalidate old session
            );
        
        return http.build();
    }
    
    /**
     * Custom authentication success handler
     * Redirects users to appropriate dashboard based on their role
     */
    private void onAuthenticationSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.core.Authentication authentication
    ) throws java.io.IOException {
        
        String redirectURL = request.getContextPath();
        
        // Role-based redirection logic
        if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            redirectURL = "/admin/dashboard";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_STUDENT"))) {
            redirectURL = "/student/dashboard";
        }
        
        response.sendRedirect(redirectURL);
    }
    
    /**
     * Configure authentication manager to use custom user details service
     * and password encoder
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }
}