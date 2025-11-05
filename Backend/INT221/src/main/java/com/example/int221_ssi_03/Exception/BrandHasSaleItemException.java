package com.example.int221_ssi_03.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BrandHasSaleItemException extends RuntimeException{
    public BrandHasSaleItemException(String message) {
        super(message);
    }
}
