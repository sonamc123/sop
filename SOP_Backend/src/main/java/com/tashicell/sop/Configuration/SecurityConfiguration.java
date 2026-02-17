package com.tashicell.sop.Configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final LogoutHandler logoutHandler;
    private final AuthenticationProvider authenticationProvider;

    // ------------------- CORS configuration -------------------
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Allow frontend NodePort origin; replace <any-node-ip> with actual node IP
        config.setAllowedOrigins(Arrays.asList("http://10.70.91.132:30080"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    // ------------------- Security filter chain -------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(); // use the CorsFilter defined above

        http
            .csrf()
            .disable()
            .authorizeHttpRequests()
                .requestMatchers(
                    "/sop/api/auth/addUser",
                    "/sop/api/auth/authenticate",
                    "/Common/**",
                    "/author/view/DownloadDocByAuthor/**",
                    "/focalPerson/downloadEndorsedSop/**",
                    "/viewer/generatePDF/**",
                    "/sopToken/**"
                )
                .permitAll()

                .requestMatchers("/author/**").hasAnyAuthority("Admin", "Author", "Reviewer","Endorser","FocalPerson", "Authoriser","Viewer")
                .requestMatchers("/reviewer/**").hasAnyAuthority("Admin", "Reviewer","Endorser","Authoriser", "FocalPerson")
                .requestMatchers("/endorser/**").hasAnyAuthority("Admin", "Endorser", "FocalPerson", "Authoriser")
                .requestMatchers("/viewer/**").hasAnyAuthority("Admin", "Author", "Reviewer","Endorser","FocalPerson","Authoriser", "Viewer")
                .requestMatchers("/UserManagement/**").hasAnyAuthority("Admin", "FocalPerson")
                .requestMatchers("/focalPerson/**").hasAnyAuthority("Admin", "Author", "FocalPerson", "Reviewer","Endorser")
                .requestMatchers("/signatureScanning/**").hasAnyAuthority("Admin", "Author","Reviewer","Endorser","FocalPerson", "Authoriser")

                .anyRequest()
                .authenticated()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout()
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());

        return http.build();
    }
}

