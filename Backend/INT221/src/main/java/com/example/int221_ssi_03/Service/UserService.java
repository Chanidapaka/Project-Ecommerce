package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.DTO.*;
import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Entities.Seller;
import com.example.int221_ssi_03.Entities.User;
import com.example.int221_ssi_03.Exception.*;
import com.example.int221_ssi_03.Repositories.SellerRepository;
import com.example.int221_ssi_03.Repositories.UserRepository;
import com.example.int221_ssi_03.utils.JwtUtils;
import com.example.int221_ssi_03.utils.TokenType;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private EmailService emailService;

    private Argon2PasswordEncoder passwordEncoder =
            new Argon2PasswordEncoder(16, 32, 8, 1024*128, 2);

    private final FileService fileService;
    private final Path userImageFolder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    public UserService(FileService fileService) {
        this.fileService = fileService;
        this.userImageFolder = fileService.initEntityFolder("user-images");
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ItemNotFoundException("User not found"));
    }

    public User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ItemNotFoundException("User not found"));
    }

    @Transactional
    public UserResponseDTO register(RegisterDTO request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DataConstraintViolationException("Email already registered");
            }

            String role = request.getUserType().toLowerCase();

            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setNickname(request.getNickName());
            user.setFullName(request.getFullName());
            user.setRole(role);
            user.setIsActive(false); // ต้องยืนยันอีเมลก่อนใช้งาน

            User saved = userRepository.saveAndFlush(user);

            if ("seller".equals(role)) {
                Seller sellerDetail = new Seller();
                sellerDetail.setUser(saved);
                sellerDetail.setMobileNumber(request.getPhoneNumber());
                sellerDetail.setBankAccountNumber(request.getBankAccount());
                sellerDetail.setBankName(request.getBankName());
                sellerDetail.setNationalId(request.getIdCardNumber());

                try {
                    String frontFileName = fileService.generateFileName(saved.getId(),
                            request.getIdCardImageFront().getOriginalFilename(), userImageFolder);
                    String backFileName = fileService.generateFileName(saved.getId(),
                            request.getIdCardImageBack().getOriginalFilename(), userImageFolder);

                    sellerDetail.setNationalFrontPhoto(
                            fileService.store(request.getIdCardImageFront(), frontFileName, userImageFolder));
                    sellerDetail.setNationalBackPhoto(
                            fileService.store(request.getIdCardImageBack(), backFileName, userImageFolder));
                } catch (Exception e) {
                    throw new DataConstraintViolationException("Failed to store seller files: " + e.getMessage());
                }

                sellerRepository.saveAndFlush(sellerDetail);
            }

            entityManager.refresh(saved);
            String token = jwtUtils.generateEmailVerifyToken(saved.getId().toString(), saved.getEmail());
            String link = frontendUrl + "/verify-email?token=" + token;

            try {
                emailService.send(saved.getEmail(), "Verify your email",
                        "Click here to verify your email: " + link);
            } catch (Exception e) {
                System.err.println("Email sending failed: " + e.getMessage());
            }

            return mapUserToDTO(saved);

        } catch (IllegalArgumentException e) {
            throw new DataConstraintViolationException("Register Failed: " + e.getMessage());
        } catch (Exception e) {
            throw new DataConstraintViolationException("Register Failed: " + e.getMessage());
        }
    }

    public UserResponseDTO verifyEmail(String token) {
        try {
            String userEmail = jwtUtils.validateAndGetUserEmail(token);
            User user = findUserByEmail(userEmail);

            if (user.getIsActive()) {
                throw new UserAlreadyActiveException("Email already verified");
            }

            user.setIsActive(true);
            return mapUserToDTO(userRepository.save(user));
        } catch (UserAlreadyActiveException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtValidationException("Invalid verification token");
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(Integer id) {
        User user = findUserById(id);

        if(!user.getIsActive()){
            throw new AccountNotActiveException("Account is not active");
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setNickName(user.getNickname());
        dto.setUserType(user.getRole());

        if ("seller".equalsIgnoreCase(user.getRole())) {
            Seller seller = sellerRepository.findByUser(user).orElse(null);
            if (seller != null) {
                dto.setPhoneNumber(maskPhone(seller.getMobileNumber()));
                dto.setBankName(seller.getBankName());
                dto.setBankAccount(maskBankAccount(seller.getBankAccountNumber()));
                dto.setNationalId(maskNationalId(seller.getNationalId()));
                dto.setIdCardImageStatus(
                        (seller.getNationalFrontPhoto() != null || seller.getNationalBackPhoto() != null)
                                ? "Provided" : null
                );
            }
        }

        return dto;
    }

    @Transactional
    public void updateProfile(Integer id, UpdateUserProfileDTO request) {
        User user = findUserById(id);

        if(!user.getIsActive()){
            throw new AccountNotActiveException("Account is not active");
        }

        boolean updated = false;

        if (request.getFullName() != null && !request.getFullName().equals(user.getFullName())) {
            user.setFullName(request.getFullName());
            updated = true;
        }
        if (request.getNickName() != null && !request.getNickName().equals(user.getNickname())) {
            user.setNickname(request.getNickName());
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
        }
    }

    public String getShippingAddress(Integer id) {
        User user = findUserById(id);
        String shippingAddress = user.getShippingAddress();
        return (shippingAddress == null || shippingAddress.isBlank()) ? "" : shippingAddress;
    }

    public void setShippingAddress(Integer id, String shippingAddress) {
        User user = findUserById(id);
        if(!user.getIsActive()){
            throw new AccountNotActiveException("Account is not active");
        }
        if (shippingAddress != null) {
            user.setShippingAddress(shippingAddress.trim());
        }
        userRepository.save(user);
    }

    public UserResponseDTO mapUserToDTO(User user) {
        UserResponseDTO userResponse = new UserResponseDTO();

        userResponse.setId(user.getId());
        userResponse.setNickName(user.getNickname());
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setActive(user.getIsActive());
        userResponse.setUserType(user.getRole());

        if ("seller".equalsIgnoreCase(user.getRole())) {
            Seller seller = sellerRepository.findByUser(user).orElse(null);
            if (seller != null) {
                userResponse.setPhoneNumber(seller.getMobileNumber());
            }
        }

        return userResponse;
    }

    public Map<String, String> login(LoginDTO userDTO) {
        try {
            UsernamePasswordAuthenticationToken upat = new
                    UsernamePasswordAuthenticationToken(
                    userDTO.getEmail(), userDTO.getPassword());
            authenticationManager.authenticate(upat);
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(userDTO.getEmail());
            if (!((AuthUserDetail) userDetails).isActive()){
                throw new AccountNotActiveException("Account is not active");
            }
            return Map.of(
                    "access_token", jwtUtils.generateToken(userDetails, TokenType.ACCESS_TOKEN),
                    "refresh_token", jwtUtils.generateToken(userDetails, TokenType.REFRESH_TOKEN)
                    );
        } catch (AccountNotActiveException e){
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Email or Password is incorrect");
        }
    }

    public Map<String, String> refreshToken(String refreshToken) {
        try{
            String email = jwtUtils.validateAndGetUserEmail(refreshToken);
            Map<String, Object> claims = jwtUtils.getJWTClaimsSet(refreshToken);
            if (! jwtUtils.isValidClaims(claims) || ! "REFRESH_TOKEN".equals(claims.get("typ"))) {
                throw new JwtValidationException("Invalid refresh token");
            }
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(claims.get("email").toString());
            if (!((AuthUserDetail) userDetails).isActive()){
                throw new AccountNotActiveException("Account is not active");
            }
            return Map.of("access_token", jwtUtils.generateToken(userDetails, TokenType.ACCESS_TOKEN));
        } catch (AccountNotActiveException e){
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token");
        }
    }


    public User findUserAndCheckActive(String email) {
        User user;

        try {
            user = findUserByEmail(email);
        }catch (Exception e) {
            throw new AuthenticationException("User not found");
        }

        if (!user.getIsActive()){
            throw new AccountNotActiveException("Account is not active");
        }

        return user;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        int len = phone.length();
        return "xxxxxx" + phone.substring(len - 4, len - 1) + "x";
    }

    private String maskBankAccount(String BankAccount) {
        if (BankAccount == null || BankAccount.length() < 4) return BankAccount;
        int len = BankAccount.length();
        return "x".repeat(len - 4) + BankAccount.substring(len - 4, len - 1) + "x";
    }

    private String maskNationalId(String id) {
        if (id == null || id.length() < 4) return id;
        int len = id.length();
        return "xxxxxxxxx" + id.substring(len - 4);
    }


    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordDTO dto) {
        User user = findUserById(userId);

        if (!user.getIsActive()) {
            throw new AccountNotActiveException("Account is not active");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new AuthenticationException("Old password is incorrect");
        }

        if (!isStrongPassword(dto.getNewPassword())) {
            throw new DataConstraintViolationException(
                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
            );
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void sendResetPasswordEmail(String email) {
        User user = findUserByEmail(email);

        if (!user.getIsActive()) {
            throw new AccountNotActiveException("Account is not active");
        }

        String token = jwtUtils.generateResetPasswordToken(user.getId().toString(), user.getEmail());

        String link = frontendUrl + "/reset-password?token=" + token;

        try {
            emailService.send(
                    user.getEmail(),
                    "Reset Your Password",
                    "Click here to reset your password: " + link + "\nThis link will expire in 15 minutes."
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        try {
            String email = jwtUtils.validateAndGetUserEmail(token);
            Map<String, Object> claims = jwtUtils.getJWTClaimsSet(token);

            if (!"RESET_PASSWORD".equals(claims.get("typ"))) {
                throw new JwtValidationException("Invalid token type");
            }

            User user = findUserByEmail(email);

            if (!user.getIsActive()) {
                throw new AccountNotActiveException("Account is not active");
            }

            if (!isStrongPassword(newPassword)) {
                throw new DataConstraintViolationException(
                        "Password must be at least 8 characters, include uppercase, lowercase, number, and special character"
                );
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

        } catch (Exception e) {
            throw new JwtValidationException("Invalid or expired token: " + e.getMessage());
        }
    }


}
