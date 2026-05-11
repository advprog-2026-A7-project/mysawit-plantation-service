package com.mysawit.plantation.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantationCodeGeneratorTest {

    private static final Pattern CODE_PATTERN = Pattern.compile("PLT-[A-F0-9]{8}");

    private final PlantationCodeGenerator generator = new PlantationCodeGenerator();

    @Test
    void generateProducesPrefixedUppercaseHexCode() {
        String code = generator.generate();

        assertNotNull(code);
        assertTrue(CODE_PATTERN.matcher(code).matches(),
                "Generated code does not match expected format: " + code);
    }

    @Test
    void generateProducesDistinctCodesAcrossInvocations() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String code = generator.generate();
            assertTrue(CODE_PATTERN.matcher(code).matches(), "Format mismatch: " + code);
            seen.add(code);
        }
        assertNotEquals(1, seen.size(), "Expected distinct codes across multiple invocations");
    }
}
