package com.ogidazepam.e_commerce.exceptions;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExceptionModel {
    private String message;
    private Instant timestamp;
    private HttpStatus status;
}
