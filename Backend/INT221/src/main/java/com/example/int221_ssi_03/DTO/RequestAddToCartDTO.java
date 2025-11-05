package com.example.int221_ssi_03.DTO;

import lombok.Data;

@Data
public class RequestAddToCartDTO {
    private Integer userId;
    private Integer saleItemId;
    private Integer quantity;
    private boolean selected;
}
