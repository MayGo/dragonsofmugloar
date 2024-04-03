package com.dragonsofmugloar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import com.dragonsofmugloar.service.ApplicationService;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class Main {
    @Autowired
    private ApplicationService applicationService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            applicationService.startApplication();
        };
    }
}