package com.tashicell.sop.Configuration;

import com.tashicell.sop.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

  private final UserRepo repository;

  @Bean
  public UserDetailsService userDetailsService() {
    return userName -> repository.findByUsername(userName)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
	CorsConfiguration configuration = new CorsConfiguration();
	//configuration.setAllowedOrigins(Arrays.asList("*"));
      //local config
      configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

    // live config
      //configuration.setAllowedOrigins(Arrays.asList("http://10.70.91.90:3000","https://sop.tashicell.com"));

	configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
	configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
	configuration.setAllowCredentials(true);
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	source.registerCorsConfiguration("/**", configuration);
	return source;
  }
}

