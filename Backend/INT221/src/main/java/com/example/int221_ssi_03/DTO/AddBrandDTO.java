package com.example.int221_ssi_03.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddBrandDTO {
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive;
}
