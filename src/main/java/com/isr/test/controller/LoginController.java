package com.isr.test.controller;

import com.isr.test.model.Login;
import com.isr.test.service.LoginService;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.*;

@RestController
public class LoginController {

	private final LoginService loginService;

	LoginController(LoginService loginService) {
		this.loginService = loginService;
	}

	@GetMapping("/logins")
	public Flux<Login> all() {

		return loginService.findAll();

	}

	@GetMapping("/test/dates")
	public Flux<LocalDate> getAllUniqueDates() {
		return loginService.getUniqueDates();
		// this will get all the records then get all the unique dates only
	}

	@GetMapping("/test/users")
	public Flux<String> getUsersByStartAndEndDate(
			@RequestParam(value = "startDate")
			@DateTimeFormat(pattern = "yyyyMMdd") LocalDate startDate,

			@RequestParam(value = "endDate")
			@DateTimeFormat(pattern = "yyyyMMdd") LocalDate endDate
	) {
		return loginService.getUsersByStartDateAndEndDate(startDate,endDate);
	}


}
