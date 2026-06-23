package com.ai.sre.healing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.ai.sre.healing", "com.ai.sre.common"})
public class HealingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealingServiceApplication.class, args);
    }
}
