/*
 * MIT License
 *
 * Copyright (c) 2025 Andrey Karazhev
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

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.promise.SettablePromise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.Map;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;

public final class AmqpPublisher extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpPublisher.class);
    private final Executor executor;
    private volatile Environment environment;
    private volatile Producer bybitStreamProducer;
    private volatile Producer bybitParserProducer;
    private volatile Producer cmcParserProducer;

    public static AmqpPublisher create(final NioReactor reactor, final Executor executor) {
        return new AmqpPublisher(reactor, executor);
    }

    private AmqpPublisher(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }

    @Override
    public Promise<?> start() {
        return Promise.ofBlocking(executor, () -> {
            try {
                environment = AmqpConfig.getEnvironment();
                bybitStreamProducer = environment.producerBuilder()
                        .name(AmqpConfig.getAmqpBybitCryptoStream())
                        .stream(AmqpConfig.getAmqpBybitCryptoStream())
                        .build();
                bybitParserProducer = environment.producerBuilder()
                        .name(AmqpConfig.getAmqpBybitParserStream())
                        .stream(AmqpConfig.getAmqpBybitParserStream())
                        .build();
                cmcParserProducer = environment.producerBuilder()
                        .name(AmqpConfig.getAmqpCmcParserStream())
                        .stream(AmqpConfig.getAmqpCmcParserStream())
                        .build();
            } catch (final Exception ex) {
                LOGGER.error("Failed to start AmqpPublisher", ex);
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Promise<?> stop() {
        return Promise.ofBlocking(executor, () -> {
            closeProducer(bybitStreamProducer);
            bybitStreamProducer = null;
            closeProducer(bybitParserProducer);
            bybitParserProducer = null;
            closeProducer(cmcParserProducer);
            cmcParserProducer = null;
            closeEnvironment();
        });
    }

    public Promise<?> publish(final Payload<Map<String, Object>> payload) {
        final var provider = payload.getProvider();
        final var source = payload.getSource();
        final var producer = getProducer(provider, source);
        if (producer == null) {
            LOGGER.debug("Skipping publish: no stream route for provider={} source={}", provider, source);
            return Promise.of(null);
        }

        final var settablePromise = new SettablePromise<Void>();
        try {
            final var message = producer.messageBuilder()
                    .addData(JsonUtils.object2Bytes(payload))
                    .build();
            producer.send(message, confirmationStatus ->
                    reactor.execute(() -> {
                        if (confirmationStatus.isConfirmed()) {
                            settablePromise.set(null);
                        } else {
                            settablePromise.setException(new RuntimeException("Stream publish not confirmed: " +
                                    confirmationStatus));
                        }
                    })
            );
        } catch (final Exception ex) {
            LOGGER.error("Failed to publish payload to stream: {}", ex.getMessage(), ex);
            settablePromise.setException(ex);
        }

        return settablePromise;
    }

    private Producer getProducer(final Provider provider, final Source source) {
        return Provider.CMC.equals(provider) ? cmcParserProducer :
                Provider.BYBIT.equals(provider) && isBybitParserSource(source) ? bybitParserProducer :
                        Provider.BYBIT.equals(provider) && isBybitStreamSource(source) ? bybitStreamProducer :
                                null;
    }

    private boolean isBybitParserSource(final Source source) {
        return Source.MD.equals(source) || Source.LPL.equals(source) || Source.LPD.equals(source) ||
                Source.BYV.equals(source) || Source.BYS.equals(source) || Source.ADH.equals(source);
    }

    private boolean isBybitStreamSource(final Source source) {
        return Source.PMST.equals(source) || Source.PML.equals(source);
    }

    private void closeProducer(final Producer producer) {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing stream producer", ex);
        }
    }

    private void closeEnvironment() {
        try {
            if (environment != null) {
                environment.close();
                environment = null;
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing stream environment", ex);
        }
    }

    public boolean isReady() {
        return environment != null
                && bybitStreamProducer != null
                && bybitParserProducer != null
                && cmcParserProducer != null;
    }
}
