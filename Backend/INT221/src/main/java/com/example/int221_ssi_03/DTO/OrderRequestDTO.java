package com.example.int221_ssi_03.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrderRequestDTO {
    private Integer buyerId;
    private Integer sellerId;
    private Instant orderDate;
    private String shippingAddress;
    private String orderNote;

    private List<OrderItem> orderItems;
    private String orderStatus;
}
