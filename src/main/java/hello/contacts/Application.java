package hello.contacts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"hello.contacts.repository"})
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
@EntityScan({"hello.contacts.model"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
