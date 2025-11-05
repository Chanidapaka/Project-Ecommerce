package com.example.int221_ssi_03.DTO;

import lombok.Data;

@Data
public class CartItemDTO {
    private Integer cartId;
    private Integer saleItemId;
    private Integer orderQuantity;
    private Integer price;
    private String description;
    private Integer quantityInStock;
    private boolean selected;
    private String image;
}
