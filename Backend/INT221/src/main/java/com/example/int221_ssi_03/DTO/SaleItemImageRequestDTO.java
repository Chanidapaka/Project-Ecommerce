package com.example.int221_ssi_03.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SaleItemImageRequestDTO {
    private Integer order;
    private String fileName;
    private String status;
    private MultipartFile imageFile;
}
