package com.abcm0018.inventarioautomatizado.auth.service;

public interface TokenBlackList {
    void addToBlackList(String token);

    boolean isBlackListed(String token);
}