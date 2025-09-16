package com.abcm0018.inventarioautomatizado.auth.service.impl;

import com.abcm0018.inventarioautomatizado.auth.service.JWTService;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {
    private static final Long SECOND_IN_A_DAY = 120000L;

    private static final Long REFRESH_SECOND_IN_A_DAY = 604800000L;

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Override
    public String getEmployeeNumberFromToken(String token){
        return getClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(User user){
        return getToken(new HashMap<>(), user);
    }

    @Override
    public String generateRefreshToken(User user){
        return getRefreshToken(new HashMap<>(), user);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String employeeNumber = getEmployeeNumberFromToken(token);
        return employeeNumber.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private String getToken(HashMap<String, Object> extractClaims, User user){
        return Jwts.builder()
                .claims(extractClaims)
                .claim("userId", user.getId())
                .claim("employeeNumber", user.getEmployeeNumber())
                .claim("name", user.getName())
                .subject(user.getEmployeeNumber())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + SECOND_IN_A_DAY))
                .signWith(getSecretKey())
                .compact();
    }

    private String getRefreshToken(HashMap<String, Object> extractClaims, User user){
        return Jwts.builder()
                .claims(extractClaims)
                .claim("userId", user.getId())
                .claim("employeeNumber", user.getEmployeeNumber())
                .claim("name", user.getName())
                .subject(user.getEmployeeNumber())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_SECOND_IN_A_DAY))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Date getExpirationDate(String token){
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token){
        return getExpirationDate(token).before(new Date());
    }
}

