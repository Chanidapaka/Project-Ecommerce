package com.example.int221_ssi_03.DTO;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BrandDTO {
    private Integer id;
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive;
    private int noOfSaleItems;
}
