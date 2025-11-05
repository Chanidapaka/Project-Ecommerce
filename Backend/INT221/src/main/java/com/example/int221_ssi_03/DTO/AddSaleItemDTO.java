package com.example.int221_ssi_03.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddSaleItemDTO {
    private Integer id;
    private String model;
    private BrandListDto brand;
    private String description;
    private Integer price;
    private Integer ramGb;
    private BigDecimal screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
    private Integer sellerId;
}
