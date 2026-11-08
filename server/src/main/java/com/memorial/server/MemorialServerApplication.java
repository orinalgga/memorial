package com.memorial.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MemorialServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemorialServerApplication.class, args);
    }
}
