package com.reservaja.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Bad Request: payload inválido / JSON inválido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        String msg = "Request body inválido: " + ex.getMostSpecificCause().getMessage();
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, msg, request.getRequestURI(), null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 400 - validação de @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex,
		    					                               HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.toList());

        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST,
            "Erro de validação",
            request.getRequestURI(),
            errors
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 400 - mismatched method arg types (ex: enum inválido, número em string)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        String message = String.format("Parâmetro inválido '%s': valor '%s' não compatível com tipo %s",
            ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");

        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), null);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 401 Authentication errors
    @ExceptionHandler({AuthenticationException.class, UnauthorizedException.class})
    public ResponseEntity<ApiError>handleAuthentication(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage() == null ? "Não autorizado" : ex.getMessage();
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, message, request.getRequestURI(), null);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 403 - Access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN, "Acesso negado", request.getRequestURI(), List.of(ex.getMessage()));
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 409 - Conflict (ex: tentativa de criar um recurso duplicado, ou sobreposição de reserva)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // 400 - BadRequestException (custom)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Cuidado: capturar Exception por último (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {
        // registrar stacktrace em logs reais (logger)
        ex.printStackTrace(); // substitua por logger.error(...)
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                                      "Erro interno no servidor",
                                      request.getRequestURI(),
                                      List.of(ex.getMessage()));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
