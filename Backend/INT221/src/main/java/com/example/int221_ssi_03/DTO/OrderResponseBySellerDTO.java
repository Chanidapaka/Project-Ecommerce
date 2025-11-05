package com.example.int221_ssi_03.DTO;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseBySellerDTO {
    private Integer id;
    private UserDTO buyer;
    private Integer sellerId;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItem> orderItems;
    private String orderStatus;
}
