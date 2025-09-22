package com.springai.video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class AiDocumentationGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDocumentationGeneratorApplication.class, args);
    }

}