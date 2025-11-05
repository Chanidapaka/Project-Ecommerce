package com.example.int221_ssi_03.DTO;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseByUserDTO {
    private Integer id;
    private Integer buyerId;
    private UserDTO seller;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItem> orderItems;
    private String orderStatus;
}
