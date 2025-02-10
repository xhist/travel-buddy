package com.travelbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TravelBuddyApplication {
	public static void main(String[] args) {
		SpringApplication.run(TravelBuddyApplication.class, args);
	}
}
