package com.example.int221_ssi_03.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccountNotActiveException extends RuntimeException{
    public AccountNotActiveException(String message) {
        super(message);
    }
}
