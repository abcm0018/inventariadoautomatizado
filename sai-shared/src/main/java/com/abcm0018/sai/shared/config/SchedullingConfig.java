package com.abcm0018.sai.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedullingConfig {
	// Spring automatically enables scheduling if it finds a @Scheduled method.
}
