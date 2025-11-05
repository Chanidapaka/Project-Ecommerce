package com.example.int221_ssi_03.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SaleItemWithImageInfo {
    private AddSaleItemDTO saleItem;
    private List<SaleItemImageRequestDTO> imageInfos;
}
