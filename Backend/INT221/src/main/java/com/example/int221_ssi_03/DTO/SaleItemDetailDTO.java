package com.example.int221_ssi_03.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
public class SaleItemDetailDTO {
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
        private Instant createdOn;
        private Instant updatedOn;
}

