package com.lab4.geometry.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.logging.Logger;

@ApplicationScoped
public class JWTUtil {
    private static final Logger logger = Logger.getLogger(JWTUtil.class.getName());

    private static final String SECRET_KEY = "lab4-super-secret-jwt-key-that-is-long-enough-for-hs256-algorithm";

    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            logger.warning("Token validation failed: " + e.getMessage());
            return null;
        }
    }
}
