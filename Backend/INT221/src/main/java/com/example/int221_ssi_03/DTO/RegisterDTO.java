package com.example.int221_ssi_03.DTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RegisterDTO {
    @NotNull(message = "nickName must not be null")
    @NotBlank(message = "nickName must not be blank")
    private String nickName;

    @NotNull(message = "email must not be null")
    @NotBlank(message = "email must not be blank")
    @Email(message = "invalid email format")
    private String email;

    @NotNull(message = "fullName must not be null")
    @NotBlank(message = "fullName must not be blank")
    private String fullName;

    @NotNull(message = "password must not be null")
    @NotBlank(message = "password must not be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "password must be at least 8 characters, include uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotNull(message = "userType must not be null")
    @NotBlank(message = "userType must not be blank")
    private String userType;

    public interface SellerGroup {}

    @NotNull(message = "phoneNumber must not be null", groups = SellerGroup.class)
    @NotBlank(message = "phoneNumber must not be blank", groups = SellerGroup.class)
    @Pattern(regexp = "^0[0-9]{9,}$", message = "phoneNumber must be 10 digits and start with 0", groups = SellerGroup.class)
    private String phoneNumber;

    @NotNull(message = "bankAccount must not be null", groups = SellerGroup.class)
    @NotBlank(message = "bankAccount must not be blank", groups = SellerGroup.class)
    @Pattern(regexp = "^[0-9]{10,16}$", message = "bankAccount must be numeric and 10 to 16 digits", groups = SellerGroup.class)
    private String bankAccount;

    @NotNull(message = "bankName must not be null", groups = SellerGroup.class)
    @NotBlank(message = "bankName must not be blank", groups = SellerGroup.class)
    private String bankName;

    @NotNull(message = "idCardNumber must not be null", groups = SellerGroup.class)
    @NotBlank(message = "idCardNumber must not be blank", groups = SellerGroup.class)
    @Pattern(regexp = "^[0-9]{13}$", message = "national ID number must be numeric and 13 digits", groups = SellerGroup.class)
    private String idCardNumber;

    private MultipartFile idCardImageFront;
    private MultipartFile idCardImageBack;
}


