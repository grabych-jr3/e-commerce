package com.ogidazepam.e_commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<ExceptionModel> productOutOfStockException(ProductOutOfStockException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionModel exceptionModel = buildExceptionModel(status, e.getMessage());
        return ResponseEntity.badRequest().body(exceptionModel);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionModel> badCredentialsException(BadCredentialsException e){
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ExceptionModel exceptionModel = buildExceptionModel(status, e.getMessage());
        return ResponseEntity.status(status).body(exceptionModel);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionModel> accessDeniedException(AccessDeniedException e){
        HttpStatus status = HttpStatus.FORBIDDEN;
        ExceptionModel exceptionModel = buildExceptionModel(status, e.getMessage());
        return ResponseEntity.status(status).body(exceptionModel);
    }

    @ExceptionHandler(OrderAlreadyPaidException.class)
    public ResponseEntity<ExceptionModel> orderAlreadyPaidException(OrderAlreadyPaidException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionModel exceptionModel = buildExceptionModel(status, e.getMessage());
        return ResponseEntity.badRequest().body(exceptionModel);
    }

    @ExceptionHandler(CartIsEmptyException.class)
    public ResponseEntity<ExceptionModel> cartIsEmptyException(CartIsEmptyException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionModel exceptionModel = buildExceptionModel(status, e.getMessage());
        return ResponseEntity.badRequest().body(exceptionModel);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionModel> resourceNotFoundException(ResourceNotFoundException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionModel exceptionModel = buildExceptionModel(status, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionModel);
    }

    private ExceptionModel buildExceptionModel(HttpStatus status, String message){
        return new ExceptionModel(
                message,
                Instant.now(),
                status
        );
    }
}