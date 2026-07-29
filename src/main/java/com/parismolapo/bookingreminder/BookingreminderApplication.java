package com.parismolapo.bookingreminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingreminderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingreminderApplication.class, args);
	}
}