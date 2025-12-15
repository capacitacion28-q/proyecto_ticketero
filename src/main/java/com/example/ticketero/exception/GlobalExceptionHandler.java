package com.example.ticketero.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manejador global de excepciones
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        log.error("Validation errors: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("Validation failed", 400, errors, LocalDateTime.now()));
    }

    /**
     * Maneja errores generales
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage(), ex);
        
        return ResponseEntity
                .status(500)
                .body(new ErrorResponse("Internal server error", 500, List.of(), LocalDateTime.now()));
    }

    /**
     * DTO para respuestas de error
     */
    public record ErrorResponse(
            String message,
            int status,
            List<String> errors,
            LocalDateTime timestamp
    ) {}
}