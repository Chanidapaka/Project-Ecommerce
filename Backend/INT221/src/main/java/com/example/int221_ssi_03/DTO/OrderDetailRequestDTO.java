package com.example.int221_ssi_03.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailRequestDTO {
    private Long saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
}
