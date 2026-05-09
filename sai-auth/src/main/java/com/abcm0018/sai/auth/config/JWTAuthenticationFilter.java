package com.abcm0018.sai.auth.config;

import com.abcm0018.sai.auth.application.JWTService;
import com.abcm0018.sai.auth.application.TokenBlackList;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private static  final String AUTH_HEADER_PREFIX = "Bearer ";
    private static final String UNAUTHORIZED_MENSAJE = "{\"error\":\"Token has been revoke\"}";

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlackList tokenBlackList;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException, ExpiredJwtException {

        final String employeeNumber;
        String token = getTokenFromRequest(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (tokenBlackList.isBlackListed(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(UNAUTHORIZED_MENSAJE);
            return;
        }
        try {
            employeeNumber = jwtService.getEmployeeNumberFromToken(token);

            // Si el usuario no se encuentra en el SecurityContexHolder
            if (employeeNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Lo recuperamos de la BD
                UserDetails userDetails = userDetailsService.loadUserByUsername(employeeNumber);
                if (jwtService.isTokenValid(token ,userDetails)) {
                    // Actualizamos el SecurityContextHolder
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(UNAUTHORIZED_MENSAJE);
        }

    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTH_HEADER_PREFIX))
            return bearerToken.substring(7);
        return null;
    }
}
