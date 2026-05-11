package com.mysawit.plantation.service;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class PlantationCodeGenerator {

    private static final String CODE_PREFIX = "PLT-";
    private static final int CODE_LENGTH = 8;

    public String generate() {
        String randomCode = UUID.randomUUID().toString().replace("-", "")
                .substring(0, CODE_LENGTH)
                .toUpperCase(Locale.ROOT);
        return CODE_PREFIX + randomCode;
    }
}
