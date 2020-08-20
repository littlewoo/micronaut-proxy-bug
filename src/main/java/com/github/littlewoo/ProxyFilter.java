package com.github.littlewoo;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.filter.*;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.runtime.server.EmbeddedServer;
import org.reactivestreams.Publisher;

@Filter("/proxy/**")
public class ProxyFilter extends OncePerRequestHttpServerFilter {
    private final ProxyHttpClient client;

    public ProxyFilter(ProxyHttpClient client, EmbeddedServer embeddedServer) {
        this.client = client;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        return Publishers.map(client.proxy(
            request.mutate()
                .uri(b -> b
                              .scheme("http")
                              .host("localhost")
                              .port(7777)
                              .replacePath(StringUtils.prependUri(
                                  "/real",
                                  request.getPath().substring("/proxy".length())
                              ))
                )
            ), response -> response);
    }
}