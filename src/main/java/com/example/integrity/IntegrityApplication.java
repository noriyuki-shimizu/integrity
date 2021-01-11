package com.example.integrity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntegrityApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntegrityApplication.class, args);
		// TODO もっといい書き方をしたい
		Confirm confirm = new Confirm();
		confirm.exec();
	}

}
