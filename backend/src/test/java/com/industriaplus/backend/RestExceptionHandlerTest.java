package com.industriaplus.backend;

import com.industriaplus.backend.error.ApiError;
import com.industriaplus.backend.error.RestExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void shouldHideUnexpectedExceptionBehindInternalErrorEnvelope() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/failure");

        ResponseEntity<ApiError> response = handler.handleUnexpectedException(
            new IllegalStateException("sensitive implementation detail"),
            request
        );

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("Ocorreu um erro interno inesperado.", response.getBody().message());
        assertEquals("/api/failure", response.getBody().path());
    }
}
