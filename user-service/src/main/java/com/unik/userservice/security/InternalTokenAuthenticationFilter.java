package com.unik.userservice.security;

import com.unik.userservice.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_HEADER = "X-Internal-Token";

    private final AppProperties appProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public InternalTokenAuthenticationFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!pathMatcher.match("/internal/**", request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(INTERNAL_HEADER);
        if (!StringUtils.hasText(token) || !token.equals(appProperties.internal().token())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid internal token\"}");
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
