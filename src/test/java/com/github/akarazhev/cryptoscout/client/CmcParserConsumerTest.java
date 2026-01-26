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

package com.github.akarazhev.cryptoscout.client;

import com.github.akarazhev.jcryptolib.cmc.parser.CmcParser;
import io.activej.reactor.nio.NioReactor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CmcParserConsumer Tests")
final class CmcParserConsumerTest {

    @BeforeAll
    static void setUp() {
        // Initialize test environment
    }

    @AfterAll
    static void tearDown() {
        // Cleanup test environment
    }

    @Test
    @DisplayName("class is loadable")
    void classIsLoadable() {
        assertDoesNotThrow(() -> Class.forName("com.github.akarazhev.cryptoscout.client.CmcParserConsumer"));
    }

    @Test
    @DisplayName("create factory method exists and is accessible")
    void createFactoryMethodExists() throws NoSuchMethodException {
        final var method = CmcParserConsumer.class.getMethod("create",
                NioReactor.class,
                CmcParser.class,
                AmqpPublisher.class);
        assertNotNull(method, "Factory method should exist");
    }

    @Test
    @DisplayName("create with null arguments throws IllegalStateException")
    void createWithNullArgumentsThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () ->
                CmcParserConsumer.create(null, null, null));
    }
}
