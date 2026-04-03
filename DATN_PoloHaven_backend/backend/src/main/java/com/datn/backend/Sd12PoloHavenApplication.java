package com.datn.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
@EnableScheduling
public class Sd12PoloHavenApplication {
	public static void main(String[] args) {
		SpringApplication.run(Sd12PoloHavenApplication.class, args);
		System.out.println("running");
	}
}
