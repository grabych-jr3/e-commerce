package com.ogidazepam.e_commerce.service.security;

import com.ogidazepam.e_commerce.util.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final String SECRET;
    private final static long VALIDITY = TimeUnit.MINUTES.toMillis(30);

    public JwtService(@Value("${jwt.secret}") String secret) {
        SECRET = secret;
    }

    public String generateToken(CustomUserDetails userDetails){
        return Jwts.builder()
                .signWith(generateKey())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(VALIDITY)))
                .compact();
    }

    private SecretKey generateKey(){
        byte[] k = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(k);
    }

    public String extractUsername(String jwt){
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    public boolean isTokenValid(String jwt){
        Claims claims = getClaims(jwt);
        return claims.getExpiration().after(Date.from(Instant.now()));
    }

    private Claims getClaims(String jwt){
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
