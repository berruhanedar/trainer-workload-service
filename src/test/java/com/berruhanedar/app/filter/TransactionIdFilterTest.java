package com.berruhanedar.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionIdFilterTest {

    private TransactionIdFilter transactionIdFilter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        transactionIdFilter = new TransactionIdFilter();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldUseExistingTransactionIdFromRequestHeader()
            throws Exception {

        String transactionId = "test-transaction-id";

        when(request.getHeader("X-Transaction-Id"))
                .thenReturn(transactionId);

        transactionIdFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(response).setHeader(
                "X-Transaction-Id",
                transactionId
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        assertNull(MDC.get("transactionId"));
    }

    @Test
    void shouldGenerateTransactionIdWhenHeaderIsMissing()
            throws Exception {

        when(request.getHeader("X-Transaction-Id"))
                .thenReturn(null);

        transactionIdFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(response).setHeader(
                eq("X-Transaction-Id"),
                argThat(value -> {
                    assertNotNull(value);
                    assertFalse(value.isBlank());
                    return true;
                })
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        assertNull(MDC.get("transactionId"));
    }

    @Test
    void shouldGenerateTransactionIdWhenHeaderIsBlank()
            throws Exception {

        when(request.getHeader("X-Transaction-Id"))
                .thenReturn("   ");

        transactionIdFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(response).setHeader(
                eq("X-Transaction-Id"),
                argThat(value -> {
                    assertNotNull(value);
                    assertFalse(value.isBlank());
                    return true;
                })
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        assertNull(MDC.get("transactionId"));
    }

    @Test
    void shouldRemoveTransactionIdFromMdcWhenFilterChainThrowsException()
            throws Exception {

        String transactionId = "test-transaction-id";

        when(request.getHeader("X-Transaction-Id"))
                .thenReturn(transactionId);

        doThrow(new RuntimeException("Test exception"))
                .when(filterChain)
                .doFilter(request, response);

        assertThrows(
                RuntimeException.class,
                () -> transactionIdFilter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertNull(MDC.get("transactionId"));
    }
}