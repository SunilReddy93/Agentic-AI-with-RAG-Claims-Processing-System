package com.sunil.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserInactiveException extends RuntimeException {

    private final HttpStatus status;

    public UserInactiveException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
