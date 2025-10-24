package com.spring.dog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.spring")
@EnableJpaRepositories(basePackages = {"com.spring.repository"})
@EntityScan(basePackages = {"com.spring.domain"})
@ComponentScan(basePackages = {"com.spring"})
public class DogApplication {
    public static void main(String[] args) {
        SpringApplication.run(DogApplication.class, args);
    }

}
