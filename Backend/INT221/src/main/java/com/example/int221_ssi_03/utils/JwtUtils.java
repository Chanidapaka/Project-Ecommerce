package com.example.int221_ssi_03.utils;

import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Exception.JwtValidationException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;

import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {
    @Value("${app.security.jwt.key-id}")
    private String KEY_ID;

    private long ACCESS_EXPIRE = 30 * 60 * 1000;
    private long REFRESH_EXPIRE = 7 * 24 * 60 * 60 * 1000;

    private RSAKey rsaPrivateJWK;
    private static RSAKey rsaPublicJWK;

    public RSAKey getRsaPublicJWK() {
        return this.rsaPublicJWK;
    }

    @PostConstruct
    public void initKeys() {
        try {
            rsaPrivateJWK = new RSAKeyGenerator(2048)
                    .keyID(KEY_ID)
                    .generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(UserDetails user, TokenType tokenType) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("http://intproj24.sit.kmutt.ac.th/ssi3/")
                    .issueTime(Date.from(now))
                    .expirationTime(new Date(new Date().getTime() + ((tokenType.equals(TokenType.ACCESS_TOKEN)) ? ACCESS_EXPIRE : REFRESH_EXPIRE)))
                    .claim("nickname", ((AuthUserDetail) user).getNickname())
                    .claim("id", ((AuthUserDetail) user).getId())
                    .claim("email", user.getUsername())
                    .claim("authorities", user.getAuthorities())
                    .claim("typ", tokenType.toString())
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(KEY_ID)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    public String generateEmailVerifyToken(String userId, String email) throws JOSEException {
        long expirationMs = 24 * 60 * 60 * 1000;
        JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("my-app")
                .expirationTime(new Date(System.currentTimeMillis() + expirationMs))
                .claim("userId", userId)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaPrivateJWK.getKeyID()).build(),
                claims
        );
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String validateAndGetUserEmail(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
            if (!signedJWT.verify(verifier)) {
                throw new JwtValidationException("Invalid signature");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(new Date())) {
                throw new JwtValidationException("Token expired");
            }

            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new JwtValidationException("Invalid JWT token");
        }
    }

    public Map<String, Object> getJWTClaimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return  signedJWT.getJWTClaimsSet().getClaims();
        } catch (ParseException ex) {
            throw new JwtValidationException("Invalid JWT (Can't parsed)");
        }
    }

    public boolean isValidClaims(Map<String, Object> jwtClaims) {
        return jwtClaims.containsKey("iat")
                && "http://intproj24.sit.kmutt.ac.th/ssi3/"
                .equals(jwtClaims.get("iss"))
                && jwtClaims.containsKey("id")
                && (Long) jwtClaims.get("id") > 0;
    }

    public String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new JwtValidationException("Missing refresh token");
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new JwtValidationException("Missing refresh token"));
    }

    public String generateResetPasswordToken(String userId, String email) {
        try {
            long expirationMs = 15 * 60 * 1000;
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issuer("my-app")
                    .expirationTime(new Date(System.currentTimeMillis() + expirationMs))
                    .claim("userId", userId)
                    .claim("typ", "RESET_PASSWORD")
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaPrivateJWK.getKeyID())
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claims
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate reset password token", e);
        }
    }
}
