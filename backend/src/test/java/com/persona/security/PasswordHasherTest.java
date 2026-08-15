package com.persona.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test
    void hashAndVerifyRoundTrip() {
        String hash = PasswordHasher.hash("secret123");
        assertTrue(PasswordHasher.verify("secret123", hash));
        assertFalse(PasswordHasher.verify("wrong", hash));
    }
}
