package com.aica.aivoca;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class AivocaApplication {

	public static void main(String[] args) {

		SpringApplication.run(AivocaApplication.class, args);
	}

}
