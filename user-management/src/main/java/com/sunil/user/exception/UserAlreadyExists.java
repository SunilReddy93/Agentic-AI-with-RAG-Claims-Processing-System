package com.sunil.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAlreadyExists extends RuntimeException{

    private final HttpStatus status;

    public UserAlreadyExists(String message, HttpStatus status){
        super(message);
        this.status = status;
    }
}
