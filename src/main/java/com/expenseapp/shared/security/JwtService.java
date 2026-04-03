package com.expenseapp.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import java.time.Instant;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtDecoder jwtDecoder;
    private final String jwtSecret;
    private final long jwtExpiration;

    public JwtService(JwtDecoder jwtDecoder, String jwtSecret, long jwtExpiration) {
        this.jwtDecoder = jwtDecoder;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(String subject, Long userId) {
        try {
            Instant now = Instant.now();
            
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer("expense-app")
                    .subject(subject)
                    .issueTime(java.util.Date.from(now))
                    .expirationTime(java.util.Date.from(now.plusSeconds(jwtExpiration)))
                    .claim("userId", userId)
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(new MACSigner(jwtSecret.getBytes()));

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public String extractUsername(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    public Long extractUserId(String token) {
        return jwtDecoder.decode(token).getClaim("userId");
    }

    public boolean isTokenValid(String token, String username) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String tokenUsername = jwt.getSubject();
            Instant expiresAt = jwt.getExpiresAt();

            return tokenUsername.equals(username) && expiresAt != null && expiresAt.isAfter(Instant.now());
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
