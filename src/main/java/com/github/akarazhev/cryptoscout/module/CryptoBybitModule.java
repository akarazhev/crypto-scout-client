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
import com.github.akarazhev.cryptoscout.client.CryptoBybitConsumer;
import com.github.akarazhev.jcryptolib.bybit.config.StreamType;
import com.github.akarazhev.jcryptolib.bybit.config.Topic;
import com.github.akarazhev.jcryptolib.bybit.stream.BybitStream;
import com.github.akarazhev.jcryptolib.bybit.stream.DataConfig;
import io.activej.http.IWebSocketClient;
import io.activej.inject.annotation.Eager;
import io.activej.inject.annotation.Named;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.akarazhev.cryptoscout.module.Constants.Config.LINEAR_BYBIT_STREAM;
import static com.github.akarazhev.cryptoscout.module.Constants.Config.SPOT_BYBIT_STREAM;

public final class CryptoBybitModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoBybitModule.class);

    private CryptoBybitModule() {
    }

    public static CryptoBybitModule create() {
        return new CryptoBybitModule();
    }

    @Provides
    @Named(LINEAR_BYBIT_STREAM)
    private BybitStream linearBybitStream(final NioReactor reactor, final IWebSocketClient webSocketClient) {
        final var config = new DataConfig.Builder()
                .streamType(StreamType.PML) // Public Mainnet Linear
                .topic(Topic.TICKERS_BTC_USDT) // tickers.BTCUSDT
                .topic(Topic.TICKERS_ETH_USDT) // tickers.ETHUSDT
                .topic(Topic.KLINE_1_BTC_USDT) // kline.1.BTCUSDT
                .topic(Topic.KLINE_1_ETH_USDT) // kline.1.ETHUSDT
                .topic(Topic.ALL_LIQUIDATION_BTC_USDT) // allLiquidation.BTCUSDT
                .topic(Topic.ALL_LIQUIDATION_ETH_USDT) // allLiquidation.ETHUSDT
                .build();
        LOGGER.info(config.print());
        return BybitStream.create(reactor, webSocketClient, config);
    }

    @Provides
    @Named(SPOT_BYBIT_STREAM)
    private BybitStream spotBybitStream(final NioReactor reactor, final IWebSocketClient webSocketClient) {
        final var config = new DataConfig.Builder()
                .streamType(StreamType.PMST) // Public Mainnet Spot
                .topic(Topic.TICKERS_BTC_USDT) // tickers.BTCUSDT
                .topic(Topic.TICKERS_ETH_USDT) // tickers.ETHUSDT
                .topic(Topic.KLINE_1_BTC_USDT) // kline.1.BTCUSDT
                .topic(Topic.KLINE_1_ETH_USDT) // kline.1.ETHUSDT
                .build();
        LOGGER.info(config.print());
        return BybitStream.create(reactor, webSocketClient, config);
    }

    @Eager
    @Provides
    private CryptoBybitConsumer cryptoBybitConsumer(final NioReactor reactor,
                                                    @Named(LINEAR_BYBIT_STREAM) final BybitStream linearBybitStream,
                                                    @Named(SPOT_BYBIT_STREAM) final BybitStream spotBybitStream,
                                                    final AmqpPublisher amqpPublisher) {
        return CryptoBybitConsumer.create(reactor, linearBybitStream, spotBybitStream, amqpPublisher);
    }
}
