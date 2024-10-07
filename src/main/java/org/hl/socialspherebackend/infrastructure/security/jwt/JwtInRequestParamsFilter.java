package org.hl.socialspherebackend.infrastructure.security.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtInRequestParamsFilter implements Filter {

    private final JwtFacade jwtFacade;
    private final UserDetailsService userDetailsService;

    public JwtInRequestParamsFilter(JwtFacade jwtFacade, UserDetailsService userDetailsService) {
        this.jwtFacade = jwtFacade;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String jwt = request.getParameter("jwt");
        if (jwt != null && !jwt.isEmpty()) {
            String username = jwtFacade.extractUsername(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, servletResponse);
    }
}
