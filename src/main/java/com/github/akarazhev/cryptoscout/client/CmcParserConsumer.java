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

import com.github.akarazhev.jcryptolib.cmc.parser.CmcParser;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Source;
import io.activej.async.service.ReactiveService;
import io.activej.datastream.consumer.StreamConsumers;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.QUOTE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.QUOTES;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.TIMESTAMP;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.TIME_CLOSE;
import static com.github.akarazhev.jcryptolib.stream.Source.BTC_USD_1D;
import static com.github.akarazhev.jcryptolib.stream.Source.BTC_USD_1W;

public final class CmcParserConsumer extends AbstractReactive implements ReactiveService {
    private final CmcParser cmcParser;
    private final AmqpPublisher amqpPublisher;

    public static CmcParserConsumer create(final NioReactor reactor, final CmcParser cmcParser,
                                           final AmqpPublisher amqpPublisher) {
        return new CmcParserConsumer(reactor, cmcParser, amqpPublisher);
    }

    private CmcParserConsumer(final NioReactor reactor, final CmcParser cmcParser,
                              final AmqpPublisher amqpPublisher) {
        super(reactor);
        this.cmcParser = cmcParser;
        this.amqpPublisher = amqpPublisher;
    }

    @Override
    public Promise<Void> start() {
        return cmcParser.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer((Payload<Map<String, Object>> payload) -> {
                    if (payload != null) {
                        if (isBtcUsdTimeframe(payload.getSource())) {
                            payload.setData(selectLatestQuote(payload.getData()));
                            amqpPublisher.publish(payload);
                        } else {
                            amqpPublisher.publish(payload);
                        }
                    }
                })));
    }

    @Override
    public Promise<Void> stop() {
        return cmcParser.stop();
    }

    private static boolean isBtcUsdTimeframe(final Source source) {
        return BTC_USD_1D.equals(source) || BTC_USD_1W.equals(source);
    }

    private Map<String, Object> selectLatestQuote(final Map<String, Object> data) {
        Map<String, Object> latest = null;
        Instant latestTs = null;
        @SuppressWarnings("unchecked") final var quotes = (List<Map<String, Object>>) data.get(QUOTES);
        for (final var quote : quotes) {
            @SuppressWarnings("unchecked") final var q = (Map<String, Object>) quote.get(QUOTE);
            if (q != null) {
                final var ts = getTimestamp((String) q.get(TIMESTAMP), (String) quote.get(TIME_CLOSE));
                if (latestTs == null || ts.isAfter(latestTs)) {
                    latestTs = ts;
                    latest = quote;
                }
            }
        }

        final var newData = new LinkedHashMap<>(data);
        final var onlyLatest = new ArrayList<>(1);
        onlyLatest.add(latest);
        newData.put(QUOTES, onlyLatest);
        return newData;
    }

    private Instant getTimestamp(final String timestamp, final String timeClose) {
        if (timestamp == null && timeClose == null) {
            return null;
        }

        return Instant.parse(timestamp != null ? timestamp : timeClose);
    }
}
