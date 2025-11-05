package com.example.int221_ssi_03.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserAlreadyActiveException extends RuntimeException{
    public UserAlreadyActiveException(String message) {
        super(message);
    }
}
