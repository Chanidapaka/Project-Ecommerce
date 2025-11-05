package com.example.int221_ssi_03.Controller;


import com.example.int221_ssi_03.DTO.LoginDTO;
import com.example.int221_ssi_03.DTO.RegisterDTO;

import com.example.int221_ssi_03.DTO.UserResponseDTO;
import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Entities.User;
import com.example.int221_ssi_03.Exception.*;
import com.example.int221_ssi_03.Service.UserService;
import com.example.int221_ssi_03.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/v2/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private Validator validator;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@ModelAttribute RegisterDTO register) {
        List<Map<String, String>> errors = new ArrayList<>();

        Set<ConstraintViolation<RegisterDTO>> violations =
                validator.validate(register);
        violations.forEach(v -> {
            Map<String, String> errMap = new LinkedHashMap<>();
            errMap.put("field", v.getPropertyPath().toString());
            errMap.put("message", v.getMessage());
            errors.add(errMap);
        });

        if (register.getUserType() != null &&
                !register.getUserType().equalsIgnoreCase("buyer") &&
                !register.getUserType().equalsIgnoreCase("seller")) {

            Map<String, String> errMap = new LinkedHashMap<>();
            errMap.put("field", "userType");
            errMap.put("message", "userType must be either 'buyer' or 'seller'");
            errors.add(errMap);
        }

        if ("seller".equalsIgnoreCase(register.getUserType())) {
            Set<ConstraintViolation<RegisterDTO>> sellerViolations =
                    validator.validate(register, RegisterDTO.SellerGroup.class);
            sellerViolations.forEach(v -> {
                Map<String, String> errMap = new LinkedHashMap<>();
                errMap.put("field", v.getPropertyPath().toString());
                errMap.put("message", v.getMessage());
                errors.add(errMap);
            });

            if (register.getIdCardImageFront() == null) {
                Map<String, String> errMap = new LinkedHashMap<>();
                errMap.put("field", "idCardImageFront");
                errMap.put("message", "must not be null");
                errors.add(errMap);
            }
            if (register.getIdCardImageBack() == null) {
                Map<String, String> errMap = new LinkedHashMap<>();
                errMap.put("field", "idCardImageBack");
                errMap.put("message", "must not be null");
                errors.add(errMap);
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationErrorException(errors);
        }


        UserResponseDTO newUser = userService.register(register);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserResponseDTO> verifyEmail(@RequestParam String token) {
        UserResponseDTO result = userService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginDTO loginRequest, HttpServletResponse response) {
        loginRequest.validate();
        Map<String, String> token = userService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", token.get("refresh_token"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, String> body = new HashMap<>();
        body.put("access_token", token.get("access_token"));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {
        String refreshToken = jwtUtils.extractRefreshToken(request);

        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @AuthenticationPrincipal AuthUserDetail authUser) {

        if (authUser == null) {
            throw new ValidationException("no access token.");
        }

        User user = userService.findUserAndCheckActive(authUser.getUsername());

        Cookie clearCookie = new Cookie("refresh_token", "");
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(true);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        userService.sendResetPasswordEmail(email);
        return ResponseEntity.ok(Map.of("message", "Reset password email has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
    }
}
