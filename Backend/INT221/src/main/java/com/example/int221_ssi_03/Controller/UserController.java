package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.*;
import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Exception.UserMismatchException;
import com.example.int221_ssi_03.Service.OrderService;
import com.example.int221_ssi_03.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v2/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}/orders")
    public ResponseEntity<PageDTO<OrderResponseByUserDTO>> getOrdersByUser(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthUserDetail authUser,
            @RequestParam Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String searchKeyword) {

        if (!authUser.getId().equals(id)) {
            throw new UserMismatchException("Cannot access other user's orders.");
        }

        return ResponseEntity.ok(orderService.getOrdersByUser(id, page, size, sortField, sortDirection, searchKeyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getProfile(
            @PathVariable Integer id,
            Authentication authentication) {
        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }

        UserProfileDTO dto = userService.getProfile(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/address")
    public ResponseEntity<Map<String,String>> getAddress(
            @PathVariable Integer id,
            Authentication authentication
    ){
        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }

        String shippingAddress = userService.getShippingAddress(id);
        return ResponseEntity.ok(Map.of("shippingAddress", shippingAddress));
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<Map<String,String>> updateAddress(
            @PathVariable Integer id,
            Authentication authentication,
            @RequestBody Map<String, String> body
    ){
        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }
        String shippingAddress = body.get("shippingAddress");
        userService.setShippingAddress(id, shippingAddress);
        return ResponseEntity.ok(Map.of("message", "Shipping address is updated successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Integer id,
            Authentication authentication,
            @ModelAttribute UpdateUserProfileDTO request) {

        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }

        userService.updateProfile(id, request);

        return ResponseEntity.ok(Map.of("message", "Profile data is updated successfully."));
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Integer id,
            Authentication authentication,
            @RequestBody ChangePasswordDTO dto
    ) {
        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }

        userService.changePassword(id, dto);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }
}
