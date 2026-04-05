package com.ecommerce.config;

import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationEntryPoint;
import com.ecommerce.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
/**
 * author: LeTuBac
 */
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource)).csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz

                        // ADMIN & PROTECTED API — ĐẶT LÊN TRÊN ĐẦU
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/support/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "SUPPORT")
                        .requestMatchers("/api/v1/user/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "CUSTOMER", "SUPPORT")
                        .requestMatchers("/api/v1/orders/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "CUSTOMER")
                        .requestMatchers("/api/v1/cart/**").hasRole("CUSTOMER").requestMatchers("/api/v1/chat/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "CUSTOMER", "SUPPORT")

                        // PUBLIC AUTH
                        .requestMatchers("/api/v1/auth/login").permitAll().requestMatchers("/api/v1/auth/register")
                        .permitAll().requestMatchers("/api/v1/auth/verify-otp").permitAll()

                        // PUBLIC DATA
                        .requestMatchers("/api/v1/products/**").permitAll().requestMatchers("/api/v1/categories/**")
                        .permitAll().requestMatchers("/api/v1/brands/**").permitAll()
                        .requestMatchers("/api/v1/files/**").permitAll().requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Còn lại phải login
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}