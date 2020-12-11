package com.forest.dataacquisitionserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
public class DataAcquisitionServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(DataAcquisitionServerApplication.class, args);
	}
}