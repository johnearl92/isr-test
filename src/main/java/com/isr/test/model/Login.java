package com.isr.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Login")
public class Login {
	@Id
	private String id;


	private Instant loginTime;
	private String user;
	private String attribute1;
	private String attribute2;
	private String attribute3;
	private String attribute4;
}