package com.example.int221_ssi_03.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponseByOrderDTO {
    private Integer id;
    private Integer buyerId;
    private SellerDetailDTO seller;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItem> orderItems;
    private String orderStatus;
}


