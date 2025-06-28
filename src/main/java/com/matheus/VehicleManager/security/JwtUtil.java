package com.matheus.VehicleManager.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000; // 1 day (in ms)

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET_KEY);
    }

    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(getAlgorithm());
    }

    public String extractUsername(String token) {
        return getDecodedJWT(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getDecodedJWT(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private DecodedJWT getDecodedJWT(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        return verifier.verify(token);
    }
}
