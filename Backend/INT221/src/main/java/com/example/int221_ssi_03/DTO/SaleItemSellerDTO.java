package com.example.int221_ssi_03.DTO;

import lombok.Data;

@Data
public class SaleItemSellerDTO {
    private Integer id;
    private String model;
    private String brandName;
    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
    private UserDTO seller;
}
