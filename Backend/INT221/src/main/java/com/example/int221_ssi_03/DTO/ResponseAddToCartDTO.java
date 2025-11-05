package com.example.int221_ssi_03.DTO;

import lombok.Data;

@Data
public class ResponseAddToCartDTO {
    private Integer cartId;
    private Integer userId;
    private Integer saleItemId;
    private Integer quantity;
    private Boolean selected;
    private String message;
}
