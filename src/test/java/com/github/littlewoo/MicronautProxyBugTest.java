package com.github.littlewoo;

import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Testing a proxy service. Starts a mock-server on localhost:7777 to represent a back-end service, with the micronaut
 * application functioning as a proxying gateway. Then sets up a route on the mock server which responds with either a
 * 200 and a given body, or a 204, and an empty body. When the response contains a body, everything works. When the
 * response contains an empty body, a read timeout occurs.
 */
@MicronautTest
public class MicronautProxyBugTest {

    @Inject
    @Client("/proxy")
    RxHttpClient microHttpClient;

    private static ClientAndServer mockServer;
    private static MockServerClient backendMock;

    @BeforeAll
    static void beforeAll() {
        mockServer = ClientAndServer.startClientAndServer(7777);
        backendMock = new MockServerClient("localhost", 7777);
    }

    @BeforeEach
    void beforeEach() {
        mockServer.reset();
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @Test
    void testEmptyBodyReturned() {
        runTest(Optional.empty());
    }

    @Test
    void testNonEmptyBodyReturned() {
        runTest(Optional.of("Hello!"));
    }

    void runTest(Optional<String> bodyToReturnFromBackend) {
        String body = "{\n  \"name\":\"joe\"\n}";
        org.mockserver.model.HttpRequest expectedRequest = request()
                                                               .withMethod("POST")
                                                               .withPath("/real/bob")
                                                               .withBody(body)
                                                               .withHeader("content-length", "" + body.length());

        org.mockserver.model.HttpResponse backendResponse = bodyToReturnFromBackend
                                                                .map(str -> response().withStatusCode(200).withBody(str))
                                                                .orElse(response().withStatusCode(204));
        backendMock.when(expectedRequest).respond(backendResponse);

        io.micronaut.http.HttpResponse response = microHttpClient.toBlocking().exchange(io.micronaut.http.HttpRequest.POST("/bob", body));
        assertEquals(backendResponse.getStatusCode(), response.getStatus().getCode());
    }
}
