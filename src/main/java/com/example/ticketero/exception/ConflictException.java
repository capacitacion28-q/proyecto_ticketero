package com.example.ticketero.exception;

/**
 * Excepción para conflictos de negocio (HTTP 409)
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}