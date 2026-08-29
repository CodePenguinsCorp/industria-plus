package com.industriaplus.backend.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
        BusinessException exception,
        HttpServletRequest request
    ) {
        return response(
            exception.getStatus(),
            exception.getCode(),
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return response(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Um ou mais campos são inválidos.",
            request,
            fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
            fieldErrors.putIfAbsent(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            )
        );

        return response(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Um ou mais campos são inválidos.",
            request,
            fieldErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "O parâmetro '%s' possui um valor inválido.".formatted(exception.getName()),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "MALFORMED_REQUEST",
            "O corpo da requisição está ausente ou contém um valor inválido.",
            request,
            Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
        DataIntegrityViolationException exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.CONFLICT,
            "DATA_CONFLICT",
            "A operação viola uma restrição de integridade dos dados.",
            request,
            Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error(
            "Unexpected error while processing {} {}",
            request.getMethod(),
            request.getRequestURI(),
            exception
        );
        return response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "Ocorreu um erro interno inesperado.",
            request,
            Map.of()
        );
    }

    private ResponseEntity<ApiError> response(
        HttpStatus status,
        String code,
        String message,
        HttpServletRequest request,
        Map<String, String> fieldErrors
    ) {
        ApiError body = new ApiError(
            OffsetDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            code,
            message,
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
