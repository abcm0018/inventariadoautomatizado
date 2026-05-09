package com.abcm0018.sai;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
public class InventarioAutomatizadoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventarioAutomatizadoApplication.class, args);
	}

}
