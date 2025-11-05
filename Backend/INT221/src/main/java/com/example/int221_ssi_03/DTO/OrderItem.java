package com.example.int221_ssi_03.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItem {
    private Integer no;
    private Long saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
}

