package com.sunil.ai.claims.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceNotAvailableException extends RuntimeException {

    private final HttpStatus status;

    public ServiceNotAvailableException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}