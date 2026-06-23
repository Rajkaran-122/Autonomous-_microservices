package com.ai.sre.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ai.sre.ai", "com.ai.sre.common"})
public class AiEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiEngineApplication.class, args);
    }
}
