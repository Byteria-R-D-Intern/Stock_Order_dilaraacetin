package com.example.stock_order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockOrderApplication.class, args);
	}

}
