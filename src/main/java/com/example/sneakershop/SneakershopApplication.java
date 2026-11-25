package com.example.sneakershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SneakershopApplication {
  // hello
  public static void main(String[] args) {
    SpringApplication.run(SneakershopApplication.class, args);
  }
}
