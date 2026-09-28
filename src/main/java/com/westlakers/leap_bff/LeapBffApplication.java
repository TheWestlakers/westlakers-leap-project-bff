package com.westlakers.leap_bff;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.westlakers.leap_bff.mappers")
public class LeapBffApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeapBffApplication.class, args);
	}

}
