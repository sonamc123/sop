package com.tashicell.sop.Configuration;

import com.tashicell.sop.Repository.TokenRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepo tokenRepository;

  @Override
  protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      if (request.getServletPath().contains("/sop/api/auth")) {
      filterChain.doFilter(request, response);
      return;
    } 
    else {
    	try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String jwt;
            final String username;
            if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
              filterChain.doFilter(request, response);
              return;
            }
            jwt = authHeader.substring(7);
            if("undefined".equals(jwt)) { 
            	return;
            }
            username = jwtService.extractUsername(jwt);
             //System.out.println(authHeader);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
              UserDetails userDetails = userDetailsService.loadUserByUsername(username);
              var isTokenValid = tokenRepository.findByToken(jwt).map(t -> !t.getExpired() && !t.getRevoked()).orElse(false);
              if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
              }
            }
            filterChain.doFilter(request, response);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
  }
}

