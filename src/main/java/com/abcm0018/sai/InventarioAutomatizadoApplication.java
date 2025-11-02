package com.abcm0018.sai;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventarioAutomatizadoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventarioAutomatizadoApplication.class, args);
	}

}
