package com.ecommerce.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * author: LeTuBac
 */
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Authentication authenticate(String usernameOrEmail, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usernameOrEmail,
                        password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public String generateToken(Authentication authentication) {
        return tokenProvider.generateToken(authentication);
    }

    public String generateRefreshToken(Authentication authentication) {
        return tokenProvider.generateRefreshToken(authentication);
    }

    public String generateRememberMeToken(Authentication authentication) {
        return tokenProvider.generateRememberMeToken(authentication);
    }

    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    public Long getUserIdFromToken(String token) {
        return tokenProvider.getUserIdFromToken(token);
    }

    public long getTokenExpirationTime(String token) {
        return tokenProvider.getTimeToExpiration(token);
    }

    public String getTokenType(String token) {
        return tokenProvider.getTokenType(token);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        } else if (authentication.getPrincipal() instanceof String) {
            try {
                return Long.valueOf((String) authentication.getPrincipal());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}