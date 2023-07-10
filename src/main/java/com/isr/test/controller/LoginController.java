package com.isr.test.controller;

import com.isr.test.model.Login;
import com.isr.test.model.SearchParameter;
import com.isr.test.service.LoginService;
import lombok.Getter;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.List;
import java.util.Map;

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
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(pattern = "yyyyMMdd") LocalDate startDate,

			@RequestParam(value = "endDate",required = false)
			@DateTimeFormat(pattern = "yyyyMMdd") LocalDate endDate
	) {
		return loginService.getUsersByStartDateAndEndDate(startDate,endDate);
	}

	@GetMapping("/test/logins")
	public Mono<Map<String, Integer>> getLogins(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(pattern = "yyyyMMdd") LocalDate startDate,

			@RequestParam(value = "endDate",required = false)
			@DateTimeFormat(pattern = "yyyyMMdd") LocalDate endDate,

			@RequestParam(value = "attribute1", required = false) List<String> attribute1,
			@RequestParam(value = "attribute2", required = false) List<String> attribute2,
			@RequestParam(value = "attribute4", required = false) List<String> attribute3,
			@RequestParam(value = "attribute4", required = false) List<String> attribute4
	) {
		SearchParameter searchParameter = ( attribute1 == null
				&& attribute2 == null
				&& attribute3 == null
				&& attribute4 == null) ? null:new SearchParameter(startDate,endDate,attribute1,attribute2,attribute3,attribute4);
		return loginService.getLoginsByParamters(searchParameter);
	}


}
