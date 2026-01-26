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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("WebConfig Tests")
final class WebConfigTest {

    @BeforeAll
    static void setUp() {
        // Initialize test environment
    }

    @AfterAll
    static void tearDown() {
        // Cleanup test environment
    }

    @Test
    @DisplayName("getServerPort returns configured port")
    void getServerPortReturnsConfiguredPort() {
        final var port = WebConfig.getServerPort();
        assertTrue(port > 0 && port <= 65535, "Port should be in valid range");
    }

    @Test
    @DisplayName("getDnsAddress returns non-null address")
    void getDnsAddressReturnsNonNullAddress() {
        final var address = WebConfig.getDnsAddress();
        assertNotNull(address, "DNS address should not be null");
    }

    @Test
    @DisplayName("getDnsTimeoutMs returns positive timeout")
    void getDnsTimeoutMsReturnsPositiveTimeout() {
        final var timeout = WebConfig.getDnsTimeoutMs();
        assertTrue(timeout > 0, "DNS timeout should be positive");
    }

    @Test
    @DisplayName("default values match application.properties")
    void defaultValuesMatchApplicationProperties() {
        assertEquals(8081, WebConfig.getServerPort());
        assertEquals("8.8.8.8", WebConfig.getDnsAddress());
        assertEquals(10000, WebConfig.getDnsTimeoutMs());
    }
}
