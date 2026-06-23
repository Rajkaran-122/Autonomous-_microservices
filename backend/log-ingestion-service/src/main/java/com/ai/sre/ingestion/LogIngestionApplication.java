package com.ai.sre.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.ai.sre.ingestion", "com.ai.sre.common"})
public class LogIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogIngestionApplication.class, args);
    }
}
