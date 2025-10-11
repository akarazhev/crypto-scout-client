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

import com.github.akarazhev.jcryptolib.bybit.stream.BybitParser;
import io.activej.async.service.ReactiveService;
import io.activej.datastream.consumer.StreamConsumers;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MetricsBybitConsumer extends AbstractReactive implements ReactiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsBybitConsumer.class);
    private final BybitParser bybitParser;
    private final AmqpPublisher amqpPublisher;

    public static MetricsBybitConsumer create(final NioReactor reactor, final BybitParser bybitParser,
                                              final AmqpPublisher amqpPublisher) {
        return new MetricsBybitConsumer(reactor, bybitParser, amqpPublisher);
    }

    private MetricsBybitConsumer(final NioReactor reactor, final BybitParser bybitParser,
                                 final AmqpPublisher amqpPublisher) {
        super(reactor);
        this.bybitParser = bybitParser;
        this.amqpPublisher = amqpPublisher;
    }

    @Override
    public Promise<?> start() {
        bybitParser.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
        LOGGER.info("MetricsBybitConsumer started");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        bybitParser.stop();
        LOGGER.info("MetricsBybitConsumer stopped");
        return Promise.complete();
    }
}
