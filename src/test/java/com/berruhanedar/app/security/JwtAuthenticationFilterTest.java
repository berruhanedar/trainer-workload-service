package com.berruhanedar.app.security;

import com.berruhanedar.app.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsNotBearer()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid()
            throws Exception {

        String token = "invalid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).isTokenValid(token);

        verify(response)
                .setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        verify(filterChain, never())
                .doFilter(request, response);

        verify(jwtService, never())
                .extractUsername(anyString());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void shouldAuthenticateAndContinueFilterChainWhenTokenIsValid()
            throws Exception {

        String token = "valid-token";
        String username = "john.smith";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUsername(token))
                .thenReturn(username);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUsername(token);

        verify(filterChain)
                .doFilter(request, response);

        verify(response, never())
                .setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(username, authentication.getPrincipal());
    }
}