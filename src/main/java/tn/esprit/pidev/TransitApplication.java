package tn.esprit.pidev;
import org.springframework.scheduling.annotation.EnableScheduling;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class TransitApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransitApplication.class, args);
    }

}
