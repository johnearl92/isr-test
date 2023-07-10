package com.isr.test.loader;

import com.isr.test.model.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LoginLoader {
	Logger logger = LoggerFactory.getLogger(LoginLoader.class);
	private final ReactiveRedisConnectionFactory factory;
	private final ReactiveRedisOperations<String, Login> loginOps;

	@Value("${test.seed.size}")
	private int seedDataSize;


	public LoginLoader(ReactiveRedisConnectionFactory factory, ReactiveRedisOperations<String, Login> loginOps) {
		this.factory = factory;
		this.loginOps = loginOps;
	}

	/***
	 * This method will load an initial data for the database
	 *
	 */
	@PostConstruct
	public void loadData() {
		Instant currentDate = Instant.now();
		Instant pastDate = currentDate.minus(30, ChronoUnit.DAYS); // Set past date as 30 days ago

		factory.getReactiveConnection().serverCommands().flushAll().thenMany(
					Flux
						.range(1, seedDataSize)
						.map(i ->
						{
							Instant loginTime = generateRandomTime(pastDate, currentDate);
							String user = "user" + i;
							return new Login(user+loginTime,
									loginTime,
									user,
									"attribute1" + i,
									"attribute2" + i,
									"attribute3" + i,
									"attribute4" + i
							);
						}

						)
						.flatMap(login -> loginOps.opsForValue().set(login.getId(), login)))
						.thenMany(loginOps.keys("*")
						.flatMap(loginOps.opsForValue()::get))
						.subscribe(login -> logger.info("Added: " + login.toString()));
	}

	private Instant generateRandomTime(Instant pastDate, Instant currentDate) {
		long pastMillis = pastDate.toEpochMilli();
		long currentMillis = currentDate.toEpochMilli();
		long randomMillisSinceEpoch = ThreadLocalRandom.current().nextLong(pastMillis, currentMillis);
		return Instant.ofEpochMilli(randomMillisSinceEpoch);
	}
}
