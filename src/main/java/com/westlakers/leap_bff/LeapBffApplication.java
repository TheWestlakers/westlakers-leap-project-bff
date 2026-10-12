package com.westlakers.leap_bff;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.westlakers.leap_bff.mappers")
@EnableScheduling
public class LeapBffApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeapBffApplication.class, args);
	}

}
