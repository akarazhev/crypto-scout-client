/*
 * MIT License
 *
 * Copyright (c) 2026 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.akarazhev.cryptoscout.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CmcConfig Tests")
final class CmcApiConfigTest {

    @BeforeAll
    static void setUp() {
        // Initialize test environment
    }

    @AfterAll
    static void tearDown() {
        // Cleanup test environment
    }

    @Test
    @DisplayName("getCmcApiKey returns non-null value")
    void getCmcApiKeyReturnsNonNullValue() {
        final var apiKey = CmcApiConfig.getCmcApiKey();
        assertNotNull(apiKey, "CMC API key should not be null (may be empty string)");
    }

    @Test
    @DisplayName("getCmcApiKey returns empty string by default")
    void getCmcApiKeyReturnsEmptyStringByDefault() {
        final var apiKey = CmcApiConfig.getCmcApiKey();
        assertEquals("", apiKey, "Default CMC API key should be empty string");
    }
}
