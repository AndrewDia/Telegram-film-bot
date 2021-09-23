package com.example.filmbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(FilmBotApplication.class, args);
		System.out.println("Bot is launched");
	}
}
