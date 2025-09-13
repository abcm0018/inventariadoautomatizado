package com.abcm0018.inventarioautomatizado.auth.service.impl;

import com.abcm0018.inventarioautomatizado.auth.service.TokenBlackList;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class InMemoryTokenBlackList implements TokenBlackList {
    private final Set<String> blackList = new HashSet<>();

    @Override
    public void addToBlackList(String token){
        blackList.add(token);
    }

    @Override
    public boolean isBlackListed(String token){
        return blackList.contains(token);
    }
}
