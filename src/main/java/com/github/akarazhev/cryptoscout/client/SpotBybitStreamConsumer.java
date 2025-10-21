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

import com.github.akarazhev.jcryptolib.bybit.stream.BybitStream;
import io.activej.async.service.ReactiveService;
import io.activej.datastream.consumer.StreamConsumers;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;

public final class SpotBybitStreamConsumer extends AbstractReactive implements ReactiveService {
    private final BybitStream spotBybitStream;
    private final AmqpPublisher amqpPublisher;

    public static SpotBybitStreamConsumer create(final NioReactor reactor, final BybitStream spotBybitStream,
                                                 final AmqpPublisher amqpPublisher) {
        return new SpotBybitStreamConsumer(reactor, spotBybitStream, amqpPublisher);
    }

    private SpotBybitStreamConsumer(final NioReactor reactor, final BybitStream spotBybitStream,
                                    final AmqpPublisher amqpPublisher) {
        super(reactor);
        this.spotBybitStream = spotBybitStream;
        this.amqpPublisher = amqpPublisher;
    }

    @Override
    public Promise<?> start() {
        return spotBybitStream.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
    }

    @Override
    public Promise<?> stop() {
        return spotBybitStream.stop();
    }
}
