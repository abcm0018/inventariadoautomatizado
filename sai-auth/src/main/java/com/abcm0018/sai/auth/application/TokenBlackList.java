package com.abcm0018.sai.auth.application;

public interface TokenBlackList {
    void addToBlackList(String token);

    boolean isBlackListed(String token);
}