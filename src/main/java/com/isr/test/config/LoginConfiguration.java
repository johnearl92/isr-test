package com.isr.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.isr.test.model.Login;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class LoginConfiguration {
	@Bean
	ReactiveRedisOperations<String, Login> redisOperations(ReactiveRedisConnectionFactory factory) {


		Jackson2JsonRedisSerializer<Login> serializer = new Jackson2JsonRedisSerializer<>(objectMapper(),Login.class);



		RedisSerializationContext.RedisSerializationContextBuilder<String, Login> builder =
				RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

		RedisSerializationContext<String, Login> context = builder.value(serializer).build();

		return new ReactiveRedisTemplate<>(factory, context);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().registerModule(new JavaTimeModule());
	}

}
