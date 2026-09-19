package com.eish.oms.config;

import static com.eish.oms.config.ApiPaths.andBelow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Basic authentication with three in-memory users (one per role) and path-based authorization.
 * Roles decide which endpoints a caller may reach; ownership of a specific order is checked in the services.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless API authenticated on every request with Basic auth: no session cookie, so CSRF does not apply.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, andBelow(ApiPaths.PRODUCTS), andBelow(ApiPaths.CATEGORIES)).permitAll()
                        .requestMatchers(andBelow(ApiPaths.ADMIN)).hasRole(Roles.ADMIN)
                        .requestMatchers(andBelow(ApiPaths.FULFILLMENT)).hasRole(Roles.STAFF)
                        .requestMatchers(andBelow(ApiPaths.CART), ApiPaths.CHECKOUT).hasRole(Roles.CUSTOMER)
                        .requestMatchers(HttpMethod.POST, ApiPaths.ORDER_RETURN_PATTERN).hasRole(Roles.CUSTOMER)
                        .requestMatchers(andBelow(ApiPaths.ORDERS)).hasAnyRole(Roles.CUSTOMER, Roles.STAFF, Roles.ADMIN)
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** The demo users from {@link DemoUsers}, with BCrypt-hashed passwords. */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(DemoUsers.ADMIN_USERNAME)
                        .password(passwordEncoder.encode(DemoUsers.ADMIN_PASSWORD))
                        .roles(Roles.ADMIN)
                        .build(),
                User.withUsername(DemoUsers.CUSTOMER_USERNAME)
                        .password(passwordEncoder.encode(DemoUsers.CUSTOMER_PASSWORD))
                        .roles(Roles.CUSTOMER)
                        .build(),
                User.withUsername(DemoUsers.STAFF_USERNAME)
                        .password(passwordEncoder.encode(DemoUsers.STAFF_PASSWORD))
                        .roles(Roles.STAFF)
                        .build());
    }
}
