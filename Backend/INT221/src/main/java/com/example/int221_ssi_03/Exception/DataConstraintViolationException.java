package com.example.int221_ssi_03.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DataConstraintViolationException extends RuntimeException {
    public DataConstraintViolationException(String message) {
        super(message);
    }
}

