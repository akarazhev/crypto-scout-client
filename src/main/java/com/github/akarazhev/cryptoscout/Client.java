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

package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.module.BybitLinearModule;
import com.github.akarazhev.cryptoscout.module.BybitSpotModule;
import com.github.akarazhev.cryptoscout.module.BybitParserModule;
import com.github.akarazhev.cryptoscout.module.ClientModule;
import com.github.akarazhev.cryptoscout.module.CmcParserModule;
import com.github.akarazhev.cryptoscout.module.CoreModule;
import com.github.akarazhev.cryptoscout.module.WebModule;
import com.github.akarazhev.jcryptolib.config.AppConfig;
import io.activej.inject.module.Module;
import io.activej.jmx.JmxModule;
import io.activej.launcher.Launcher;
import io.activej.service.ServiceGraphModule;

import java.util.LinkedList;

import static com.github.akarazhev.cryptoscout.Constants.Module.CMC_PARSER_MODULE_ENABLED;
import static com.github.akarazhev.cryptoscout.Constants.Module.BYBIT_STREAM_MODULE_ENABLED;
import static com.github.akarazhev.cryptoscout.Constants.Module.BYBIT_PARSER_MODULE_ENABLED;
import static io.activej.inject.module.Modules.combine;

final class Client extends Launcher {

    @Override
    protected Module getModule() {
        final var modules = new LinkedList<Module>();
        modules.add(JmxModule.create());
        modules.add(ServiceGraphModule.create());
        modules.add(CoreModule.create());
        modules.add(ClientModule.create());

        if (AppConfig.getAsBoolean(BYBIT_PARSER_MODULE_ENABLED)) {
            modules.add(BybitParserModule.create());
        }

        if (AppConfig.getAsBoolean(BYBIT_STREAM_MODULE_ENABLED)) {
            modules.add(BybitSpotModule.create());
            modules.add(BybitLinearModule.create());
        }

        if (AppConfig.getAsBoolean(CMC_PARSER_MODULE_ENABLED)) {
            modules.add(CmcParserModule.create());
        }

        modules.add(WebModule.create());
        return combine(modules);
    }

    @Override
    protected void run() throws Exception {
        awaitShutdown();
    }

    static void main(final String[] args) throws Exception {
        new Client().launch(args);
    }
}
