package com.example.int221_ssi_03.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    private Integer buyerId;
    private SellerDetailDTO seller;
    private List<CartItemDTO> cartItems;
}
