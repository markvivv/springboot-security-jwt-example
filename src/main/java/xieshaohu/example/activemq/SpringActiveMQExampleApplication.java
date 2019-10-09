package xieshaohu.example.activemq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringActiveMQExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringActiveMQExampleApplication.class, args);
	}

}
