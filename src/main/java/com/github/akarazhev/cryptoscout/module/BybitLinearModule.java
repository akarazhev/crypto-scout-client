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
import com.github.akarazhev.cryptoscout.client.BybitLinearStreamConsumer;
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

import static com.github.akarazhev.cryptoscout.module.Constants.Config.BYBIT_LINEAR_STREAM;

public final class BybitLinearModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitLinearModule.class);

    private BybitLinearModule() {
    }

    public static BybitLinearModule create() {
        return new BybitLinearModule();
    }

    @Provides
    @Named(BYBIT_LINEAR_STREAM)
    private BybitStream bybitLinearStream(final NioReactor reactor, final IWebSocketClient webSocketClient) {
        final var config = new DataConfig.Builder()
                .streamType(StreamType.PML) // Public Mainnet Linear
                .topic(Topic.KLINE_60_BTC_USDT) // kline.60.BTCUSDT
                .topic(Topic.KLINE_60_ETH_USDT) // kline.60.ETHUSDT
                .topic(Topic.TICKERS_BTC_USDT) // tickers.BTCUSDT
                .topic(Topic.TICKERS_ETH_USDT) // tickers.ETHUSDT
                .topic(Topic.PUBLIC_TRADE_BTC_USDT) // publicTrade.BTCUSDT
                .topic(Topic.PUBLIC_TRADE_ETH_USDT) // publicTrade.ETHUSDT
                .topic(Topic.ORDER_BOOK_200_BTC_USDT) // orderbook.200.BTCUSDT
                .topic(Topic.ORDER_BOOK_200_ETH_USDT) // orderbook.200.ETHUSDT
                .topic(Topic.ALL_LIQUIDATION_BTC_USDT) // allLiquidation.BTCUSDT
                .topic(Topic.ALL_LIQUIDATION_ETH_USDT) // allLiquidation.ETHUSDT
                .build();
        LOGGER.info(config.print());
        return BybitStream.create(reactor, webSocketClient, config);
    }

    @Eager
    @Provides
    private BybitLinearStreamConsumer bybitLinearStreamConsumer(final NioReactor reactor,
                                                                @Named(BYBIT_LINEAR_STREAM) final BybitStream bybitLinearStream,
                                                                final AmqpPublisher amqpPublisher) {
        return BybitLinearStreamConsumer.create(reactor, bybitLinearStream, amqpPublisher);
    }
}
