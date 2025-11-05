package com.example.int221_ssi_03.Exception;

import java.util.List;
import java.util.Map;

public class ValidationErrorException extends RuntimeException{
    private final List<Map<String, String>> errors;

    public ValidationErrorException(List<Map<String, String>> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public List<Map<String, String>> getErrors() {
        return errors;
    }
}
