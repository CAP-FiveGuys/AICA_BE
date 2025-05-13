package com.aica.aivoca;


import com.aica.aivoca.wordinfo.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
public class AivocaApplication {

	public static void main(String[] args) {

		SpringApplication.run(AivocaApplication.class, args);
	}

}
