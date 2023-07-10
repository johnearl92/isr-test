package com.isr.test.service;

import com.isr.test.loader.LoginLoader;
import com.isr.test.model.Login;
import com.isr.test.model.SearchParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Range;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoginService {
    private ReactiveRedisOperations<String, Login> redisOperations;

    Logger logger = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    public LoginService(ReactiveRedisOperations<String, Login> redisOperations) {
        this.redisOperations = redisOperations;
    }


    public Flux<Login> findAll() {
        return redisOperations.keys("*")
                .flatMap(redisOperations.opsForValue()::get);
    }

    public Flux<LocalDate> getUniqueDates() {
        return redisOperations.keys("*").flatMap(redisOperations.opsForValue()::get)
                .map(login -> login.getLoginTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .distinct().sort();
    }

    /***
     * This will get the users that has record of login between startDate and endDate.
     * @param startDate
     * @param endDate
     * @return
     */
    public Flux<String> getUsersByStartDateAndEndDate(LocalDate startDate, LocalDate endDate) {

        return getLoginWithInRange(startDate,endDate)
                .map(Login::getUser)
                .distinct().sort();
    }

    public Flux<Login> getLoginWithInRange(LocalDate startDate, LocalDate endDate) {

        // implement a scan option to better handle the reading in the table
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match("*")
                .count(1000) // Add count(1000) to limit the number of keys retrieved per scan iteration this will improve usage of memory and network sources
                .build();

        return redisOperations
                .scan(scanOptions)
                .flatMap(redisOperations.opsForValue()::get)
                .filter(login -> isWithinRange(login.getLoginTime(), startDate, endDate));
    }

    private boolean isWithinRange(Instant loginTime, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            LocalDate loginDate = loginTime.atOffset(ZoneOffset.UTC).toLocalDate();
            return !loginDate.isBefore(startDate) && !loginDate.isAfter(endDate);
        }
        return true;
    }

    public Mono<Map<String, Integer>> getLoginsByParamters(SearchParameter searchParameter) {
        if (searchParameter == null) {
            return Mono.just(Collections.emptyMap());
        }

        logger.info("starting search for Logins from "+searchParameter.getStartDate());

        getLoginWithInRange(searchParameter.getStartDate(),searchParameter.getEndDate())
                .subscribe(System.out::println);


        return getLoginWithInRange(searchParameter.getStartDate(),searchParameter.getEndDate())
                .filter(login -> {
                    String attribute1 = login.getAttribute1();
                    String attribute2 = login.getAttribute2();
                    String attribute3 = login.getAttribute3();
                    String attribute4 = login.getAttribute4();

                    return evaluateFilter(attribute1, searchParameter.getAttribute1())
                            && evaluateFilter(attribute2, searchParameter.getAttribute2())
                            && evaluateFilter(attribute3, searchParameter.getAttribute3())
                            && evaluateFilter(attribute4, searchParameter.getAttribute4());
                })
                .groupBy(Login::getUser)
                .flatMap(group -> group.count().map(count -> Map.entry(group.key(), count.intValue())))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);


    }

    public boolean evaluateFilter(String loginAtt,List<String> searchParamAtt) {
        if (searchParamAtt != null ) {
            return searchParamAtt.contains(loginAtt);
        } else {
            return true;
        }
    }
}
