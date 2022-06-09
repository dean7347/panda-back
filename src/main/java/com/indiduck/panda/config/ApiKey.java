package com.indiduck.panda.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApiKey {
    @Value("${RESTAPIKEY}")
    private String RESTAPIKEY;

    @Value("${RESTAPISECRET}")
    private String RESTAPISECRET;

    private static ApiKey instance = new ApiKey();

    private ApiKey() {
    }

    public static ApiKey getInstance() {
        if (instance == null) {
            instance = new ApiKey();
        }
        return instance;
    }
}