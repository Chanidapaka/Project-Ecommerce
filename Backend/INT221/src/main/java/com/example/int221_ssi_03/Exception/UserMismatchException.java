package com.example.int221_ssi_03.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UserMismatchException extends RuntimeException{
    public UserMismatchException(String message) {
        super(message);
    }
}
