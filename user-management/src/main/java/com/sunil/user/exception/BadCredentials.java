package com.sunil.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadCredentials extends RuntimeException {

    private final HttpStatus status;

    public BadCredentials(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
