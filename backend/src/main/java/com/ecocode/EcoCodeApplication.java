package com.ecocode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcoCodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcoCodeApplication.class, args);
    }
}
