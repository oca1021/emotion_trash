package com.ggomi.emotion_trash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EmotionTrashApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmotionTrashApplication.class, args);
	}

}
