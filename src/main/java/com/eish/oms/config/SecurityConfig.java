package com.eish.oms.config;

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

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_STAFF = "STAFF";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless API authenticated on every request with Basic auth: no session cookie, so CSRF does not apply.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/api/fulfillment/**").hasRole(ROLE_STAFF)
                        .requestMatchers("/api/cart/**", "/api/checkout").hasRole(ROLE_CUSTOMER)
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/return").hasRole(ROLE_CUSTOMER)
                        .requestMatchers("/api/orders/**").hasAnyRole(ROLE_CUSTOMER, ROLE_STAFF, ROLE_ADMIN)
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Demo users. A real deployment would replace this with a user store or an identity provider;
     * the path rules above would not change.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(passwordEncoder.encode("admin123")).roles(ROLE_ADMIN).build(),
                User.withUsername("customer").password(passwordEncoder.encode("customer123")).roles(ROLE_CUSTOMER).build(),
                User.withUsername("staff").password(passwordEncoder.encode("staff123")).roles(ROLE_STAFF).build());
    }
}
