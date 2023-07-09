package com.isr.test;

import com.isr.test.controller.LoginController;
import com.isr.test.model.Login;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
//  We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Mock
    private ReactiveRedisOperations<String, Login> loginOps;

    @Mock
    private ReactiveValueOperations<String, Login> valueOperations;

    @InjectMocks
    private LoginController loginController;

    @Test
    public void testGetAll() {
        // Make a request to your endpoint that returns Flux<Login>
        Flux<Login> loginFlux = webTestClient.get()
                .uri("/logins")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Login.class)
                .getResponseBody();

        // Assert the response body
        webTestClient.get()
                .uri("/logins")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Login.class)
                .isEqualTo(loginFlux.collectList().block());
    }

    @Test
    public void testGetUniqueDates() {
        // Mock login dates
        Instant instant1 = Instant.parse("2023-07-01T00:00:00Z");
        Instant instant2 = Instant.parse("2023-07-02T00:00:00Z");
        Instant instant3 = Instant.parse("2023-07-02T00:00:00Z");

        // Expected unique dates
        List<LocalDate> expectedDates = Arrays.asList(
                LocalDate.parse("2023-07-01"),
                LocalDate.parse("2023-07-02")
        );

        when(loginOps.keys("*"))
                .thenReturn(Flux.just(
                        "login:1",
                        "login:2",
                        "login:3"
                ));

        when(loginOps.opsForValue())
                .thenReturn(valueOperations);

        when(loginOps.opsForValue().get("login:1"))
                .then(invocation -> Flux.just(createLogin("login:1",instant1)));
        when(loginOps.opsForValue().get("login:2"))
                .then(invocation -> Flux.just(createLogin("login:2",instant2)));
        when(loginOps.opsForValue().get("login:3"))
                .then(invocation -> Flux.just(createLogin("login:3",instant3)));


        // Perform the HTTP request and verify the response
        webTestClient.get().uri("/test/dates")
                .exchange()
                .expectStatus().isOk();

        // TODO
//                .expectBodyList(LocalDate.class)
//                .value(response -> {
//                    List<LocalDate> actualDates = response;
//                    // Assert that the dates are unique
//                    assertThat(actualDates).containsExactlyElementsOf(expectedDates);
//                });

    }

    private Login createLogin(String id, Instant instant) {
        return new Login(id, instant, null, null, null, null, null);
    }


}


