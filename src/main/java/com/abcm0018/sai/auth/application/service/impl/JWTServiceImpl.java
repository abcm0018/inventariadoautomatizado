package com.abcm0018.sai.auth.application.service.impl;

import com.abcm0018.sai.auth.application.JWTService;
import com.abcm0018.sai.users.domain.entity.User;
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

    @Value("${jwt.secret.key}")
    private String secretKey;

	@Value("${jwt.access-token.expiration:3600000}")
	private Long accessTokenExpiration;

	@Value("${jwt.refresh-token.expiration:604800000}")
	private Long refreshTokenExpiration;

    @Override
    public String getEmployeeNumberFromToken(String token){
        return getClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(User user){
        return getToken(new HashMap<>(), user, accessTokenExpiration);
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

    private String getToken(HashMap<String, Object> extractClaims, User user, Long expiration) {
		long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extractClaims)
                .claim("userId", user.getId())
                .claim("employeeNumber", user.getEmployeeNumber())
                .claim("name", user.getName())
                .subject(user.getEmployeeNumber())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(currentTimeMillis + expiration))
                .signWith(getSecretKey())
                .compact();
    }

    private String getRefreshToken(HashMap<String, Object> extractClaims, User user) {
        return getToken(extractClaims, user, refreshTokenExpiration);
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getAllClaims(String token) {
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

