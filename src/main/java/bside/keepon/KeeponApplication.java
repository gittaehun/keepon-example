package bside.keepon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KeeponApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeeponApplication.class, args);
	}

}
