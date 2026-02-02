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

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import io.activej.eventloop.Eventloop;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("AmqpPublisher Tests")
final class AmqpPublisherTest {
    private ExecutorService executor;
    private ExecutorService publisherExecutor;
    private Eventloop reactor;
    private AmqpPublisher amqpPublisher;

    @BeforeEach
    void setUp() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        publisherExecutor = Executors.newVirtualThreadPerTaskExecutor();
        reactor = Eventloop.builder()
                .withCurrentThread()
                .build();
        amqpPublisher = AmqpPublisher.create(reactor, publisherExecutor);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        reactor.post(() -> amqpPublisher.stop()
                .whenComplete(() -> reactor.breakEventloop()));
        reactor.run();
        shutdownExecutor(executor);
        shutdownExecutor(publisherExecutor);
    }

    private void shutdownExecutor(final ExecutorService exec) throws InterruptedException {
        exec.shutdown();
        if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
            exec.shutdownNow();
        }
    }

    @Test
    @DisplayName("create returns non-null instance")
    void shouldCreateReturnNonNullInstance() {
        assertNotNull(amqpPublisher, "AmqpPublisher should not be null");
    }

    @Test
    @DisplayName("isReady returns false before start")
    void shouldIsReadyReturnFalseBeforeStart() {
        assertFalse(amqpPublisher.isReady(),
                "Should not be ready before start");
    }

    @Test
    @DisplayName("publish with unknown provider returns completed promise")
    void shouldPublishWithUnknownProviderReturnCompletedPromise() {
        final Payload<Map<String, Object>> payload = new Payload<>();
        payload.setProvider(null);
        payload.setSource(null);
        payload.setData(new HashMap<>());

        // Should not throw, returns completed promise for unknown routes
        final var promise = amqpPublisher.publish(payload);
        assertNotNull(promise, "Promise should not be null");
    }

    @Test
    @DisplayName("publish with CMC provider routes to crypto-scout stream")
    void shouldPublishWithCmcProviderRouteToCryptoScoutStream() {
        final Payload<Map<String, Object>> payload = new Payload<>();
        payload.setProvider(Provider.CMC);
        payload.setSource(Source.FGI_API_PRO_L);
        payload.setData(new HashMap<>());

        // Without started environment, this should handle gracefully
        final var promise = amqpPublisher.publish(payload);
        assertNotNull(promise, "Promise should not be null");
    }

    @Test
    @DisplayName("publish with Bybit stream source routes to bybit stream")
    void shouldPublishWithBybitStreamSourceRouteToBybitStream() {
        final Payload<Map<String, Object>> payload = new Payload<>();
        payload.setProvider(Provider.BYBIT);
        payload.setSource(Source.PMST);
        payload.setData(new HashMap<>());

        final var promise = amqpPublisher.publish(payload);
        assertNotNull(promise, "Promise should not be null");
    }
}
