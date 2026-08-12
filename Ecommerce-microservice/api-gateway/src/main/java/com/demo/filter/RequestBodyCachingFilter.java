package com.demo.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestBodyCachingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // Apply caching only for login endpoint
        if (!exchange.getRequest()
                .getURI()
                .getPath()
                .equals("/api/users/login")) {
            return chain.filter(exchange);
        }

        // ─────────────────────────────────────────────────
        // Cache the request body so it can be read
        // multiple times — once by KeyResolver for rate
        // limiting and once by the downstream Auth Service
        // ─────────────────────────────────────────────────
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {

                    // read all bytes
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    // wrap bytes so they can be re-read multiple times
                    Flux<DataBuffer> cachedBody = Flux.defer(() ->
                            Mono.just(exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(bytes))
                    );

                    // replace request with cached body version
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                            exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedBody;
                        }
                    };

                    return chain.filter(
                            exchange.mutate()
                                    .request(mutatedRequest)
                                    .build()
                    );
                });
    }

    @Override
    public int getOrder() {
        return -2;//run before JWT filter and rate limiter
    }
}
