package com.isr.test;

import com.isr.test.controller.LoginController;
import com.isr.test.model.Login;
import com.isr.test.model.SearchParameter;
import com.isr.test.service.LoginService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@WebFluxTest
class LoginTest {

    @Autowired
    private WebTestClient webTestClient;

    @Mock
    private ReactiveRedisOperations<String, Login> redisOperations;

    @Mock
    private ReactiveValueOperations<String, Login> valueOperations;

    @InjectMocks
    private LoginService loginService;

    @MockBean
    private LoginController loginController;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");



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
    void testGetLoginsWithEmptyParameters() {

        // given
        Login login1 = createLogin("1", "2023-07-01T10:00:00Z", "testuser1");
        Login login2 = createLogin("2", "2023-07-02T11:00:00Z", "testuser2");
        Login login3 = createLogin("3", "2023-07-02T12:00:00Z", "testuser3");
        Flux<String> ids = Flux.just("1","2","3");

        // when
        when(redisOperations.keys(Mockito.any())).thenReturn(ids);
        when(redisOperations.scan(Mockito.any())).thenReturn(ids);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        when(redisOperations.opsForValue().get("1")).thenReturn(Mono.just(login1));
        when(redisOperations.opsForValue().get("2")).thenReturn(Mono.just(login2));
        when(redisOperations.opsForValue().get("3")).thenReturn(Mono.just(login3));

        // then
        webTestClient.get()
                .uri("/test/logins")
                .exchange()
                .expectStatus().isOk();

        Mono<Map<String,Integer>> emptyMono = loginService.getLoginsByParameters(null);
        StepVerifier.create(emptyMono)
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();

    }

    @Test
    void testGetLoginsBySearchParameter(){
        // given
        Login login1 = createLogin("1", "2023-07-01T10:00:00Z", "testuser1","att1","att1","att1","att1");
        Login login2 = createLogin("2", "2023-07-02T11:00:00Z", "testuser2","att1",null,null,null);
        Login login3 = createLogin("3", "2023-07-03T12:00:00Z", "testuser1","att2","att1","att1","att1");
        Flux<String> ids = Flux.just("1","2","3");

        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("testuser1", 1);
        expectedMap.put("testuser2", 1);

        // when
        when(redisOperations.keys(Mockito.any())).thenReturn(ids);
        when(redisOperations.scan(Mockito.any())).thenReturn(ids);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        when(redisOperations.opsForValue().get("1")).thenReturn(Mono.just(login1));
        when(redisOperations.opsForValue().get("2")).thenReturn(Mono.just(login2));
        when(redisOperations.opsForValue().get("3")).thenReturn(Mono.just(login3));

        // then
        webTestClient.get()
                .uri("/test/logins?startDate=20230701&endDate=20230704&attribute1=att1")
                .exchange()
                .expectStatus().isOk();

        SearchParameter searchParameter = new SearchParameter(LocalDate.parse("20230701",dateTimeFormatter),
                LocalDate.parse("20230704",dateTimeFormatter),
                List.of("att1"),
                null,
                null,
                null);

        Mono<Map<String,Integer>> result = loginService.getLoginsByParameters(searchParameter);
        StepVerifier.create(result)
                .expectNext(expectedMap)
                .verifyComplete();

    }

    @Test
    void testGetLoginsBySearchParameterWithMultipleAtt1() {
        // given
        Login login1 = createLogin("1", "2023-07-01T10:00:00Z", "testuser1","att1","att1","att1","att1");
        Login login2 = createLogin("2", "2023-07-02T11:00:00Z", "testuser2","att1",null,null,null);
        Login login3 = createLogin("3", "2023-07-03T12:00:00Z", "testuser1","att2","att1","att1","att1");
        Flux<String> ids = Flux.just("1","2","3");

        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("testuser1", 2);
        expectedMap.put("testuser2", 1);

        // when
        when(redisOperations.keys(Mockito.any())).thenReturn(ids);
        when(redisOperations.scan(Mockito.any())).thenReturn(ids);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        when(redisOperations.opsForValue().get("1")).thenReturn(Mono.just(login1));
        when(redisOperations.opsForValue().get("2")).thenReturn(Mono.just(login2));
        when(redisOperations.opsForValue().get("3")).thenReturn(Mono.just(login3));

        // then
        webTestClient.get()
                .uri("/test/logins?startDate=20230701&endDate=20230704&attribute1=att1&attribute1=att2")
                .exchange()
                .expectStatus().isOk();
//
        SearchParameter searchParameter = new SearchParameter(LocalDate.parse("20230701",dateTimeFormatter),
                LocalDate.parse("20230704",dateTimeFormatter),
                List.of("att1","att2"),
                null,
                null,
                null);
//
        Mono<Map<String,Integer>> result = loginService.getLoginsByParameters(searchParameter);
        StepVerifier.create(result)
                .expectNext(expectedMap)
                .verifyComplete();
    }

    @Test
    void testGetLoginsBySearchParameterWithAtt1AndAtt2() {
        // given
        Login login1 = createLogin("1", "2023-07-01T10:00:00Z", "testuser1","att1","att1","att1","att1");
        Login login2 = createLogin("2", "2023-07-02T11:00:00Z", "testuser2","att1","att1",null,null);
        Login login3 = createLogin("3", "2023-07-03T12:00:00Z", "testuser1","att1","att3","att1","att1");
        Flux<String> ids = Flux.just("1","2","3");

        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("testuser1", 1);
        expectedMap.put("testuser2", 1);

        // when
        when(redisOperations.keys(Mockito.any())).thenReturn(ids);
        when(redisOperations.scan(Mockito.any())).thenReturn(ids);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        when(redisOperations.opsForValue().get("1")).thenReturn(Mono.just(login1));
        when(redisOperations.opsForValue().get("2")).thenReturn(Mono.just(login2));
        when(redisOperations.opsForValue().get("3")).thenReturn(Mono.just(login3));

        // then
        webTestClient.get()
                .uri("/test/logins?startDate=20230701&endDate=20230704&attribute1=att1&attribute1=att2")
                .exchange()
                .expectStatus().isOk();
//
        SearchParameter searchParameter = new SearchParameter(LocalDate.parse("20230701",dateTimeFormatter),
                LocalDate.parse("20230704",dateTimeFormatter),
                List.of("att1"),
                List.of("att1"),
                null,
                null);
//
        Mono<Map<String,Integer>> result = loginService.getLoginsByParameters(searchParameter);
        StepVerifier.create(result)
                .expectNext(expectedMap)
                .verifyComplete();
    }

    @Test
    public void testGetUniqueDates() {
        // given
        Login login1 = createLogin("1", "2023-07-01T10:00:00Z", "testuser1");
        Login login2 = createLogin("2", "2023-07-02T11:00:00Z", "testuser2");
        Login login3 = createLogin("3", "2023-07-02T12:00:00Z", "testuser3");
        Flux<String> ids = Flux.just("1","2","3");
        // when
        when(redisOperations.keys(Mockito.any())).thenReturn(ids);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        when(redisOperations.opsForValue().get("1")).thenReturn(Mono.just(login1));
        when(redisOperations.opsForValue().get("2")).thenReturn(Mono.just(login2));
        when(redisOperations.opsForValue().get("3")).thenReturn(Mono.just(login3));

        // then
        webTestClient.get()
                .uri("/test/dates")
                .exchange()
                .expectStatus().isOk();

        Flux<LocalDate> actualDates = loginService.getUniqueDates();
        StepVerifier.create(actualDates)
                .expectNext(LocalDate.parse("20230701",dateTimeFormatter),LocalDate.parse("20230702",dateTimeFormatter))
                .expectComplete()
                .verify();
    }

    @Test
    public void testGetUniqueUsersByStartDateAndEndDate() {
        // Mock login data
        Login login1 = createLogin("1", "2023-07-01T10:00:00Z", "testuser1");
        Login login2 = createLogin("2", "2023-07-02T11:00:00Z", "testuser2");
        Login login3 = createLogin("3", "2023-07-03T12:00:00Z", "testuser3");
        Login login4 = createLogin("4", "2023-07-04T13:00:00Z", "testuser4");
        Login login5 = createLogin("5", "2023-07-05T14:00:00Z", "testuser5");
        Flux<String> ids = Flux.just("1","2","3","4","5");

        // Mock Redis operations
        when(redisOperations.scan(Mockito.any())).thenReturn(ids);
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        when(redisOperations.opsForValue().get("1")).thenReturn(Mono.just(login1));
        when(redisOperations.opsForValue().get("2")).thenReturn(Mono.just(login2));
        when(redisOperations.opsForValue().get("3")).thenReturn(Mono.just(login3));
        when(redisOperations.opsForValue().get("4")).thenReturn(Mono.just(login4));
        when(redisOperations.opsForValue().get("5")).thenReturn(Mono.just(login5));


        // Perform the test
        webTestClient.get()
                .uri("/test/users?startDate=20230702&endDate=20230704")
                .exchange()
                .expectStatus().isOk();

       Flux<String> users = loginService.getUsersByStartDateAndEndDate(LocalDate.parse("20230702",dateTimeFormatter),
                LocalDate.parse("20230704",dateTimeFormatter));
        StepVerifier.create(users)
                .expectNext("testuser2", "testuser3", "testuser4")
                .expectComplete()
                .verify();

    }

    private Login createLogin(String id, String text, String testuser1) {
        return createLogin(id,text,testuser1,"","","","");
    }

    private Login createLogin(String id, String text, String testuser1, String attribute1, String attribute2, String attribute3, String attribute4 ) {
        return new Login(id, Instant.parse(text), testuser1, attribute1, attribute2, attribute3, attribute4);
    }
}


