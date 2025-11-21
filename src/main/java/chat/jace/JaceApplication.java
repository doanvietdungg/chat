package chat.jace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaceApplication.class, args);
	}

}
