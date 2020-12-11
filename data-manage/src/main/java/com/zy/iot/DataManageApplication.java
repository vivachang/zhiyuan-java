package com.zy.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DataManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataManageApplication.class, args);
    }

}
