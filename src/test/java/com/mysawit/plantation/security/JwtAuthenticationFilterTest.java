package com.mysawit.plantation.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsWhenAuthorizationHeaderMissing() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
        verifyNoInteractions(jwtUtil);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void skipsWhenTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        FilterChain filterChain = mock(FilterChain.class);
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(jwtUtil).validateToken("invalid-token");
        verify(jwtUtil, never()).extractClaims(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void setsAuthenticationForValidTokenWithRole() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        FilterChain filterChain = mock(FilterChain.class);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-1");
        when(claims.get("role", String.class)).thenReturn("ADMIN");

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractClaims("valid-token")).thenReturn(claims);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user-1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(List.of("ROLE_ADMIN"),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .toList());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doesNotReplaceExistingAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        FilterChain filterChain = mock(FilterChain.class);
        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertSame(existingAuthentication, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).extractClaims(anyString());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doesNotAuthenticateWhenRoleMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        FilterChain filterChain = mock(FilterChain.class);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-1");
        when(claims.get("role", String.class)).thenReturn(null);

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractClaims("valid-token")).thenReturn(claims);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void continuesWhenClaimExtractionThrows() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractClaims("valid-token")).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }
}
