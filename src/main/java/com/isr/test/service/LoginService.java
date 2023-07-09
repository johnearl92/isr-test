package com.isr.test.service;

import com.isr.test.model.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Range;

@Component
public class LoginService {
    private ReactiveRedisOperations<String, Login> redisOperations;

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

    public Flux<String> getUsersByStartDateAndEndDate(LocalDate startDate, LocalDate endDate) {

        return redisOperations.keys("*").flatMap(redisOperations.opsForValue()::get)
                .filter(login -> isWithinRange(login.getLoginTime(), startDate, endDate))
                .map(Login::getUser)
                .distinct().sort();
    }

    private boolean isWithinRange(Instant loginTime, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            LocalDate loginDate = loginTime.atOffset(ZoneOffset.UTC).toLocalDate();
            return !loginDate.isBefore(startDate) && !loginDate.isAfter(endDate);
        }
        return true;
    }

}