package com.example.int221_ssi_03.DTO;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String nickName;
    private String userType;

    private String phoneNumber;
    private String bankName;
    private String bankAccount;
    private String nationalId;
    private String idCardImageStatus;
}
