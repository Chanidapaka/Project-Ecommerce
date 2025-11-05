package com.example.int221_ssi_03.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class SellerNotFoundException  extends RuntimeException{
    public SellerNotFoundException(String message) {
        super(message);
    }
}
