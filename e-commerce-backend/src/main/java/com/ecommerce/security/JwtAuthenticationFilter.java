package com.ecommerce.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
/**
 * author: LeTuBac
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// Skip JWT validation for public endpoints
		String path = request.getRequestURI();
		logger.info("🔎 Filter processing path: " + path);
		if (shouldNotFilter(path)) {
			chain.doFilter(request, response);
			return;
		}

		final String requestTokenHeader = request.getHeader("Authorization");
		logger.debug("📨 Authorization header received: " + (requestTokenHeader != null ? "Yes" : "No"));

		Long userId = null;
		String jwtToken = null;

		// JWT Token is in the form "Bearer token". Remove Bearer word and get only the
		// Token
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			jwtToken = requestTokenHeader.substring(7);
			try {
				// Validate token first
				if (jwtTokenProvider.validateToken(jwtToken)) {
					userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
					logger.debug("✅ JWT Token validated, userId extracted: " + userId);
				} else {
					logger.warn("❌ JWT Token validation failed");
				}
			} catch (Exception e) {
				logger.warn("⚠️ JWT Token extraction failed: " + e.getMessage());
			}
		} else {
			logger.warn("⚠️ No valid Authorization header found or Bearer token missing");
		}

		// Once we get the userId, load user and set authentication
		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				// Load user by ID from database to get fresh role/authorities
				UserDetails userDetails = this.userDetailsService.loadUserById(userId);

				// Set authentication in context with authorities
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

				logger.debug("✅ User authenticated: " + userId + " with authorities: " + userDetails.getAuthorities());
			} catch (Exception e) {
				logger.warn("❌ JWT Authentication failed: " + e.getMessage());
			}
		} else {
			if (userId == null) {
				logger.warn("⚠️ Could not extract userId from JWT token");
			} else if (SecurityContextHolder.getContext().getAuthentication() != null) {
				logger.debug("⏭️ Authentication already exists in context, skipping");
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * Determine if JWT validation should be skipped for certain paths
	 */
	private static final Set<String> PUBLIC_ENDPOINTS = Set.of("/api/v1/auth/login", "/api/v1/auth/register",
			"/api/v1/auth/refresh");

	private boolean shouldNotFilter(String path) {
		boolean skip = PUBLIC_ENDPOINTS.contains(path)
				|| path.startsWith("/api/public/")
				|| path.startsWith("/ws/")
				|| path.startsWith("/actuator/");

		logger.info("⛔ Skip filter? {} = {}" + path + ", " + skip);
		return skip;
	}
}