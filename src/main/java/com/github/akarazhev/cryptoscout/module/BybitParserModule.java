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

package com.github.akarazhev.cryptoscout.module;

import com.github.akarazhev.cryptoscout.client.AmqpPublisher;
import com.github.akarazhev.cryptoscout.client.BybitParserConsumer;
import com.github.akarazhev.jcryptolib.bybit.config.Type;
import com.github.akarazhev.jcryptolib.bybit.stream.BybitParser;
import com.github.akarazhev.jcryptolib.bybit.stream.DataConfig;
import io.activej.http.IHttpClient;
import io.activej.inject.annotation.Eager;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BybitParserModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitParserModule.class);

    private BybitParserModule() {
    }

    public static BybitParserModule create() {
        return new BybitParserModule();
    }

    @Provides
    private BybitParser bybitParser(final NioReactor reactor, final IHttpClient httpClient) {
        final var config = new DataConfig.Builder()
                .type(Type.MD) // Mega Drop
                .type(Type.LPL) // Launch Pool
                .type(Type.LPD) // Launchpad
                .type(Type.BYV) // ByVotes Spot
                .type(Type.BYS) // ByStarter
                .type(Type.ADH) // Airdrop Hunt
                .build();
        LOGGER.info(config.print());
        return BybitParser.create(reactor, httpClient, config);
    }

    @Eager
    @Provides
    private BybitParserConsumer bybitParserConsumer(final NioReactor reactor, final BybitParser bybitParser,
                                                    final AmqpPublisher amqpPublisher) {
        return BybitParserConsumer.create(reactor, bybitParser, amqpPublisher);
    }
}
