package com.example.int221_ssi_03.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponseDTO {
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
    private SaleItemDetailDTO saleItemDetailDTO;
}

