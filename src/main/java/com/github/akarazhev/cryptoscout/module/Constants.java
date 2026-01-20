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

package com.github.akarazhev.cryptoscout.module;

final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    final static class Config {
        private Config() {
            throw new UnsupportedOperationException();
        }

        static final String BYBIT_SPOT_BTC_USDT_STREAM = "bybitSpotBtcUsdtStream";
        static final String BYBIT_SPOT_ETH_USDT_STREAM = "bybitSpotEthUsdtStream";
        static final String BYBIT_LINEAR_BTC_USDT_STREAM = "bybitLinearBtcUsdtStream";
        static final String BYBIT_LINEAR_ETH_USDT_STREAM = "bybitLinearEthUsdtStream";
    }

    final static class API {
        private API() {
            throw new UnsupportedOperationException();
        }

        static final String OK_RESPONSE = "ok";
        static final String HEALTH_API = "/health";
        static final String NOT_READY_RESPONSE = "not-ready";
    }

    final static class HttpCode {
        private HttpCode() {
            throw new UnsupportedOperationException();
        }

        static final int NOT_READY = 503;
    }
}
