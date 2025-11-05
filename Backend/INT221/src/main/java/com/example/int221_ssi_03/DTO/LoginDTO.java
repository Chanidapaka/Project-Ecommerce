package com.example.int221_ssi_03.DTO;

import com.example.int221_ssi_03.Exception.ValidationException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    private String email;
    private String password;

    public void validate() {
        // ตรวจสอบ email
        if (email == null || email.isBlank() || email.length() > 50
                || !email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("Email or Password is incorrect");
        }

        // ตรวจสอบ password
        if (password == null || password.isEmpty() || password.length() > 14)
        {
            throw new ValidationException("Email or Password is incorrect");
        }
    }
}
