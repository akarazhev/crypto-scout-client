# Context: Implementation of the limit to filter the `BTC/USD` pairs of 1D and 1W timeframes in `CmcParserConsumer.java`

In this `crypto-scout-client` project let's implement the limit to only filter the latest `Payload` data that represents
as `BTC/USD` pairs of 1D and 1W timeframes.

## Roles

Take the following roles:

- Expert java developer.

## Conditions

- Rely on the current implementation of the `CmcParserConsumer.java` service.
- Double-check your proposal and make sure that they are correct and haven't missed any important points.
- Implementation must be production ready.
- Use the best practices and design patterns.

## Constraints

- Use the current technological stack, that's: `Java 25`, `ActiveJ 6.0-rc2`.
- Follow the current code style.
- Do not hallucinate.

## Tasks

- As the expert java developer update the `CmcParserConsumer.java` service by implementing the filter and limit
  mechanism for the latest `Payload` data that represents as `BTC/USD` pairs of 1D and 1W timeframes. Filter the latest
  `Payload` data that represents as `BTC/USD` pairs of 1D and 1W timeframes by `"source": "BTC_USD_1W"` and 
  `"source": "BTC_USD_1D"`. Check samples below.
- As the `expert java engineer` double-check your proposal and make sure that they are correct and haven't missed any
  important points.

## Implementation of `CmcParserConsumer` service that streams data between `CmcParser` and `AmqpPublisher` services

```java

@Override
public Promise<?> start() {
    return cmcParser.start().then(stream ->
            stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
}
```

## Implementation of the `Payload` model that represents the data received from the `coinmarketcap` API

```java
package com.github.akarazhev.jcryptolib.stream;

public final class Payload<T> {
    private Provider provider;
    private Source source;
    private T data;

    public Payload() {
    }

    public static <T> Payload<T> of(Provider provider, Source source, T data) {
        return new Payload<T>(provider, source, data);
    }

    private Payload(Provider provider, Source source, T data) {
        this.provider = provider;
        this.source = source;
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public Provider getProvider() {
        return this.provider;
    }

    public Source getSource() {
        return this.source;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String toString() {
        String var10000 = String.valueOf(this.data);
        return "Payload{data=" + var10000 + ", provider=" + String.valueOf(this.provider) + ", source=" + String.valueOf(this.source) + "}";
    }
}
```

Data for `BTC/USD` pairs of 1D and 1W timeframes.

```json
{
  "provider": "CMC",
  "source": "BTC_USD_1W",
  "data": {
    "id": 1,
    "name": "Bitcoin",
    "symbol": "BTC",
    "timeEnd": "1522627199",
    "quotes": [
      {
        "timeOpen": "2018-04-02T00:00:00.000Z",
        "timeClose": "2018-04-08T23:59:59.999Z",
        "timeHigh": "2018-04-03T19:39:28.000Z",
        "timeLow": "2018-04-06T11:04:27.000Z",
        "quote": {
          "name": "2781",
          "open": 6844.8598632813,
          "high": 7530.9399414063,
          "low": 6575.0000000000,
          "close": 7023.5200195313,
          "volume": 3652499968.0000000000,
          "marketCap": 119162880482.0000000000,
          "circulatingSupply": 16966262,
          "timestamp": "2018-04-08T23:59:59.999Z"
        }
      },
      {
        "timeOpen": "2018-04-09T00:00:00.000Z",
        "timeClose": "2018-04-15T23:59:59.999Z",
        "timeHigh": "2018-04-15T14:14:34.000Z",
        "timeLow": "2018-04-09T20:24:27.000Z",
        "quote": {
          "name": "2781",
          "open": 7044.3198242188,
          "high": 8338.4199218750,
          "low": 6661.9902343750,
          "close": 8329.1103515625,
          "volume": 5244480000.0000000000,
          "marketCap": 141427138383.0000000000,
          "circulatingSupply": 16979862,
          "timestamp": "2018-04-15T23:59:59.999Z"
        }
      }
    ]
  }
}
```