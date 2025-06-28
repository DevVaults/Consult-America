package consult_america.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import consult_america.demo.model.Role;
import consult_america.demo.repository.RoleRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository repo) {
        return args -> {
            if (repo.findByName("ADMIN").isEmpty()) {
                repo.save(new Role("ADMIN"));
            }
            if (repo.findByName("CANDIDATE").isEmpty()) {
                repo.save(new Role("CANDIDATE"));
            }
        };
    }

}
