package com.example.demo;

import com.example.demo.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final UserService userService;

        @Bean
        public UserDetailsService userDetailsService() {
                return new UserService();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                var auth = new DaoAuthenticationProvider(userDetailsService());
                auth.setPasswordEncoder(passwordEncoder());
                return auth;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/", "/oauth/**",
                                                                "/register",
                                                                "/error", "/403", "/products",
                                                                "/cart", "/cart/**", "/momo/**")
                                                .permitAll()
                                                .requestMatchers("/admin/products/**", "/admin/products")
                                                .hasAnyAuthority("ADMIN", "MANAGER")
                                                .requestMatchers("/admin/**")
                                                .hasAnyAuthority("ADMIN")
                                                .requestMatchers("/point-exchange/**")
                                                .hasAnyAuthority("USER")
                                                .requestMatchers("/api/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .rememberMe(rememberMe -> rememberMe
                                                .key("hutech")
                                                .rememberMeCookieName("hutech")
                                                .tokenValiditySeconds(24 * 60 * 60)
                                                .userDetailsService(userDetailsService()))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .accessDeniedPage("/403"))
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .maximumSessions(1)
                                                .expiredUrl("/login"))
                                .httpBasic(httpBasic -> httpBasic
                                                .realmName("hutech"))
                                .build();
        }
}