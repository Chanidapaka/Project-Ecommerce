package com.example.int221_ssi_03.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer id;
    private String nickName;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean isActive;
    private String userType;

}
