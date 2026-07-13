package com.isoft.medalterguardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedalterguardianApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedalterguardianApplication.class, args);
    }

}
