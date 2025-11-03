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
import com.github.akarazhev.cryptoscout.client.BybitLinearBtcUsdtConsumer;
import com.github.akarazhev.cryptoscout.client.BybitLinearEthUsdtConsumer;
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

import static com.github.akarazhev.cryptoscout.module.Constants.Config.BYBIT_LINEAR_BTC_USDT_STREAM;
import static com.github.akarazhev.cryptoscout.module.Constants.Config.BYBIT_LINEAR_ETH_USDT_STREAM;

public final class BybitLinearModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitLinearModule.class);

    private BybitLinearModule() {
    }

    public static BybitLinearModule create() {
        return new BybitLinearModule();
    }

    @Provides
    @Named(BYBIT_LINEAR_BTC_USDT_STREAM)
    private BybitStream bybitLinearBtcUsdtStream(final NioReactor reactor, final IWebSocketClient webSocketClient) {
        final var config = new DataConfig.Builder()
                .streamType(StreamType.PML) // Public Mainnet Linear
                .topic(Topic.KLINE_15_BTC_USDT) // kline.15.BTCUSDT
                .topic(Topic.KLINE_60_BTC_USDT) // kline.60.BTCUSDT
                .topic(Topic.KLINE_240_BTC_USDT) // kline.240.BTCUSDT
                .topic(Topic.KLINE_D_BTC_USDT) // kline.D.BTCUSDT
                .topic(Topic.TICKERS_BTC_USDT) // tickers.BTCUSDT
                .topic(Topic.PUBLIC_TRADE_BTC_USDT) // publicTrade.BTCUSDT
                .topic(Topic.ORDER_BOOK_50_BTC_USDT) // orderbook.50.BTCUSDT
                .topic(Topic.ORDER_BOOK_200_BTC_USDT) // orderbook.200.BTCUSDT
                .topic(Topic.ORDER_BOOK_1000_BTC_USDT) // orderbook.1000.BTCUSDT
                .topic(Topic.ALL_LIQUIDATION_BTC_USDT) // allLiquidation.BTCUSDT
                .build();
        LOGGER.info(config.print());
        return BybitStream.create(reactor, webSocketClient, config);
    }

    @Provides
    @Named(BYBIT_LINEAR_ETH_USDT_STREAM)
    private BybitStream bybitLinearEthUsdtStream(final NioReactor reactor, final IWebSocketClient webSocketClient) {
        final var config = new DataConfig.Builder()
                .streamType(StreamType.PML) // Public Mainnet Linear
                .topic(Topic.KLINE_15_ETH_USDT) // kline.15.ETHUSDT
                .topic(Topic.KLINE_60_ETH_USDT) // kline.60.ETHUSDT
                .topic(Topic.KLINE_240_ETH_USDT) // kline.240.ETHUSDT
                .topic(Topic.KLINE_D_ETH_USDT) // kline.D.ETHUSDT
                .topic(Topic.TICKERS_ETH_USDT) // tickers.ETHUSDT
                .topic(Topic.PUBLIC_TRADE_ETH_USDT) // publicTrade.ETHUSDT
                .topic(Topic.ORDER_BOOK_50_ETH_USDT) // orderbook.50.ETHUSDT
                .topic(Topic.ORDER_BOOK_200_ETH_USDT) // orderbook.200.ETHUSDT
                .topic(Topic.ORDER_BOOK_1000_ETH_USDT) // orderbook.1000.ETHUSDT
                .topic(Topic.ALL_LIQUIDATION_ETH_USDT) // allLiquidation.ETHUSDT
                .build();
        LOGGER.info(config.print());
        return BybitStream.create(reactor, webSocketClient, config);
    }

    @Eager
    @Provides
    private BybitLinearBtcUsdtConsumer bybitLinearBtcUsdtConsumer(final NioReactor reactor,
                                                                  @Named(BYBIT_LINEAR_BTC_USDT_STREAM)
                                                                  final BybitStream bybitLinearBtcUsdtStream,
                                                                  final AmqpPublisher amqpPublisher) {
        return BybitLinearBtcUsdtConsumer.create(reactor, bybitLinearBtcUsdtStream, amqpPublisher);
    }

    @Eager
    @Provides
    private BybitLinearEthUsdtConsumer bybitLinearEthUsdtConsumer(final NioReactor reactor,
                                                                  @Named(BYBIT_LINEAR_ETH_USDT_STREAM)
                                                                  final BybitStream bybitLinearEthUsdtStream,
                                                                  final AmqpPublisher amqpPublisher) {
        return BybitLinearEthUsdtConsumer.create(reactor, bybitLinearEthUsdtStream, amqpPublisher);
    }
}
