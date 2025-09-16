package com.abcm0018.inventarioautomatizado.shared.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;


public class JWTUtils {

    private JWTUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static String extractTokenFromRequest(WebRequest request){
        // Get the Authorization header from the request
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        //Check if the Authorization header is not null and starts with "Bearer"
        if(StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer")){
            //Extract the JWT token (remove "Bearer" prefix)
            return authorizationHeader.substring(7);
        }
        //If the Authorization header is not valid, return null
        return null;
    }

}
