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

package com.github.akarazhev.cryptoscout.config;

import com.github.akarazhev.jcryptolib.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_BYBIT_STREAM;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_CRYPTO_SCOUT_STREAM;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_HOST;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_PASSWORD;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_USERNAME;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_PORT;
import static com.github.akarazhev.cryptoscout.config.Constants.CmcConfig.CMC_API_KEY;
import static com.github.akarazhev.cryptoscout.config.Constants.WebConfig.DNS_ADDRESS;
import static com.github.akarazhev.cryptoscout.config.Constants.WebConfig.DNS_TIMEOUT_MS;
import static com.github.akarazhev.cryptoscout.config.Constants.WebConfig.SERVER_PORT;

public final class ConfigValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigValidator.class);

    private ConfigValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validate(final boolean cmcModuleEnabled) throws IllegalStateException {
        final var missing = new ArrayList<String>();

        validateAmqpConfig(missing);
        validateWebConfig(missing);

        if (cmcModuleEnabled) {
            validateCmcConfig(missing);
        }

        if (!missing.isEmpty()) {
            final var message = "Missing required configuration properties: " + missing;
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        LOGGER.info("Configuration validation passed");
    }

    private static void validateAmqpConfig(final List<String> missing) {
        validateRequired(AMQP_RABBITMQ_HOST, missing);
        validateHostname(AMQP_RABBITMQ_HOST, missing);
        validateRequired(AMQP_RABBITMQ_USERNAME, missing);
        validateRequired(AMQP_RABBITMQ_PASSWORD, missing);
        validateRequiredIntRange(AMQP_STREAM_PORT, missing, 1, 65535);
        validateRequired(AMQP_BYBIT_STREAM, missing);
        validateRequired(AMQP_CRYPTO_SCOUT_STREAM, missing);
    }

    private static void validateWebConfig(final List<String> missing) {
        validateRequiredIntRange(SERVER_PORT, missing, 1, 65535);
        validateRequired(DNS_ADDRESS, missing);
        validateHostname(DNS_ADDRESS, missing);
        validateRequiredIntRange(DNS_TIMEOUT_MS, missing, 100, 60000);
    }

    private static void validateCmcConfig(final List<String> missing) {
        validateRequired(CMC_API_KEY, missing);
    }

    private static void validateRequired(final String key, final List<String> missing) {
        final var value = AppConfig.getAsString(key);
        if (value == null || value.isBlank()) {
            missing.add(key);
        }
    }

    private static void validateRequiredInt(final String key, final List<String> missing) {
        try {
            final var value = AppConfig.getAsInt(key);
            if (value <= 0) {
                missing.add(key + " (must be positive)");
            }
        } catch (final Exception e) {
            missing.add(key);
        }
    }

    private static void validateRequiredIntRange(final String key, final List<String> missing,
                                                  final int min, final int max) {
        try {
            final var value = AppConfig.getAsInt(key);
            if (value < min || value > max) {
                missing.add(key + " (must be between " + min + " and " + max + ")");
            }
        } catch (final Exception e) {
            missing.add(key);
        }
    }

    private static void validateHostname(final String key, final List<String> missing) {
        final var value = AppConfig.getAsString(key);
        if (value != null && !value.isBlank()) {
            if (!isValidHostname(value)) {
                missing.add(key + " (invalid hostname or IP address)");
            }
        }
    }

    private static boolean isValidHostname(final String hostname) {
        // Simple validation for IPv4 addresses or hostnames
        return hostname.matches("^(([0-9]{1,3}\\.){3}[0-9]{1,3})|([a-zA-Z0-9.-]+)$");
    }
}
