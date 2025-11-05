package com.example.int221_ssi_03.filters;

import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Exception.JwtValidationException;
import com.example.int221_ssi_03.Service.JwtUserDetailsService;
import com.example.int221_ssi_03.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtUtils jwtUtils  ;

    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response
            , FilterChain chain) throws ServletException, IOException {
        try{
            response.setHeader("request-uri", request.getRequestURI());
            final String requestTokenHeader = request.getHeader("Authorization");
            String userEmail = null;
            String jwtToken = null;
            Map<String, Object> claims = null;
            if (requestTokenHeader != null) {
                if (requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    jwtUtils.validateAndGetUserEmail(jwtToken);
                    claims = jwtUtils.getJWTClaimsSet(jwtToken);
                    if (! jwtUtils.isValidClaims(claims) || ! "ACCESS_TOKEN".equals(claims.get("typ"))) {
                        throw new JwtValidationException("Invalid JWT access token");
                    }
                    userEmail = claims.get("email").toString();
                } else {
                    throw new JwtValidationException("JWT Token does not begin with Bearer String");
                }
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(userEmail);
                if(userDetails == null || ! userDetails.getUsername().equals(claims.get("email"))) {
                    throw new JwtValidationException("Invalid JWT Token");
                }
                UsernamePasswordAuthenticationToken upAuthToken = new UsernamePasswordAuthenticationToken(((AuthUserDetail) userDetails), null, userDetails.getAuthorities());
                upAuthToken.setDetails(claims);
                SecurityContextHolder.getContext().setAuthentication(upAuthToken);
                authentication = SecurityContextHolder.getContext().getAuthentication();
            }
            chain.doFilter(request, response);
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
