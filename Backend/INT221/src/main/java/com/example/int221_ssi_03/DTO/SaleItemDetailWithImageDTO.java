package com.example.int221_ssi_03.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class SaleItemDetailWithImageDTO {
    private Integer id;
    private String model;
    private String brandName;
    private String description;
    private Integer price;
    private Integer ramGb;
    private BigDecimal screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
    private List<SaleItemImageDTO> saleItemImages;
    private Instant createdOn;
    private Instant updatedOn;
}
