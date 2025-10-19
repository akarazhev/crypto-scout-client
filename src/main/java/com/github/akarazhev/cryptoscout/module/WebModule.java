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

import com.github.akarazhev.cryptoscout.config.WebConfig;
import com.github.akarazhev.jcryptolib.bybit.config.Config;
import io.activej.dns.DnsClient;
import io.activej.dns.IDnsClient;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpClient;
import io.activej.http.HttpMethod;
import io.activej.http.HttpResponse;
import io.activej.http.HttpServer;
import io.activej.http.IHttpClient;
import io.activej.http.IWebSocketClient;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Eager;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import io.activej.reactor.Reactor;
import io.activej.reactor.nio.NioReactor;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.module.Constants.Config.DEFAULT_DNS;
import static com.github.akarazhev.cryptoscout.module.Constants.Config.HEALTH_API;
import static com.github.akarazhev.cryptoscout.module.Constants.Config.OK_RESPONSE;
import static io.activej.http.HttpUtils.inetAddress;

/**
 * Http module. Http server + routing. Fully async (Promise-based).
 */
public final class WebModule extends AbstractModule {

    private WebModule() {
    }

    public static WebModule create() {
        return new WebModule();
    }

    @Provides
    private IDnsClient dnsClient(final NioReactor reactor) {
        return DnsClient.builder(reactor, inetAddress(DEFAULT_DNS))
                .build();
    }

    @Provides
    private IWebSocketClient webSocketClient(final NioReactor reactor, final IDnsClient dnsClient, final Executor executor)
            throws NoSuchAlgorithmException {
        return HttpClient.builder(reactor, dnsClient)
                .withConnectTimeout(Duration.ofMillis(Config.getConnectTimeoutMs()))
                .withSslEnabled(SSLContext.getDefault(), executor)
                .build();
    }

    @Provides
    private IHttpClient httpClient(final NioReactor reactor, final IDnsClient dnsClient, final Executor executor)
            throws NoSuchAlgorithmException {
        return HttpClient.builder(reactor, dnsClient)
                .withConnectTimeout(Duration.ofMillis(Config.getConnectTimeoutMs()))
                .withSslEnabled(SSLContext.getDefault(), executor)
                .build();
    }

    @Provides
    private AsyncServlet servlet(final Reactor reactor) {
        return RoutingServlet.builder(reactor)
                .with(HttpMethod.GET, HEALTH_API, (request) ->
                        HttpResponse.ok200().withPlainText(OK_RESPONSE).toPromise())
                .build();
    }

    @Provides
    @Eager
    private HttpServer server(final NioReactor reactor, final AsyncServlet servlet) {
        return HttpServer.builder(reactor, servlet)
                .withListenPort(WebConfig.getServerPort())
                .build();
    }
}
