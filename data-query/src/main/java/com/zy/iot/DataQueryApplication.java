package com.zy.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class DataQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataQueryApplication.class, args);
    }

}
