package com.example.stock_order.adapters.web.exception;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.stock_order.adapters.web.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger SECURE_LOG =
            org.slf4j.LoggerFactory.getLogger("SECURE");

    private ErrorResponse body(HttpStatus status, String message, String path, Map<String, Object> details) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCred(InvalidCredentialsException ex, HttpServletRequest req) {
        SECURE_LOG.warn("Invalid credentials at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body(HttpStatus.UNAUTHORIZED, "invalid_credentials", req.getRequestURI(), null));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(AccountLockedException ex, HttpServletRequest req) {
        Instant now = Instant.now();
        long remainingSec = Math.max(0, ex.getLockedUntil().getEpochSecond() - now.getEpochSecond());
        long remainingMin = Math.max(0, Duration.ofSeconds(remainingSec).toMinutes());

        String msg = "Hesabınız 15 dakika kilitlendi. " + ex.getLockedUntil()
                + " tarihine kadar giriş yapamazsınız"
                + (remainingMin > 0 ? (" (~" + remainingMin + " dk kaldı)") : "");

        Map<String, Object> details = Map.of("lockedUntil", ex.getLockedUntil());
        SECURE_LOG.warn("Account locked at {} : lockedUntil={}", req.getRequestURI(), ex.getLockedUntil());
        return ResponseEntity.status(423).body(body(HttpStatus.valueOf(423), msg, req.getRequestURI(), details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> detailMap = new LinkedHashMap<>();
        for (var error : ex.getBindingResult().getAllErrors()) {
            if (error instanceof FieldError fe) {
                Object rejected = fe.getRejectedValue();
                if ("password".equalsIgnoreCase(fe.getField())) {
                    rejected = "***";
                }
                detailMap.put(fe.getField(), Map.of(
                        "message", fe.getDefaultMessage(),
                        "rejectedValue", rejected
                ));
            } else {
                detailMap.put(error.getObjectName(), error.getDefaultMessage());
            }
        }
        SECURE_LOG.info("Validation failed (body) at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "validation_failed", req.getRequestURI(), detailMap));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> detailMap = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            detailMap.put(v.getPropertyPath().toString(), Map.of(
                    "message", v.getMessage(),
                    "rejectedValue", v.getInvalidValue()
            ));
        }
        SECURE_LOG.info("Validation failed (params) at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "validation_failed", req.getRequestURI(), detailMap));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest req) {
        Map<String, Object> detailMap = new LinkedHashMap<>();
        for (var error : ex.getBindingResult().getFieldErrors()) {
            detailMap.put(error.getField(), Map.of(
                    "message", error.getDefaultMessage(),
                    "rejectedValue", error.getRejectedValue()
            ));
        }
        SECURE_LOG.info("Bind error at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "validation_failed", req.getRequestURI(), detailMap));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        SECURE_LOG.warn("Bad request at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "bad_request", req.getRequestURI(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        SECURE_LOG.warn("Illegal argument at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "bad_request", req.getRequestURI(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppNotFound(NotFoundException ex, HttpServletRequest req) {
        SECURE_LOG.warn("Not found at {} : {}", req.getRequestURI(), ex.toString());
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "not_found";
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body(HttpStatus.NOT_FOUND, "not_found", req.getRequestURI(), null));
    }

    @ExceptionHandler({
        org.springframework.security.access.AccessDeniedException.class,
        org.springframework.security.authorization.AuthorizationDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest req) {
        SECURE_LOG.warn("Access denied at {} : {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body(HttpStatus.FORBIDDEN, "access_denied", req.getRequestURI(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        String path = req.getRequestURI();

        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            throw new RuntimeException(ex);
        }

        SECURE_LOG.error("Unhandled exception at {} : {}", path, ex.toString(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected_error", path, null));
    }
}
