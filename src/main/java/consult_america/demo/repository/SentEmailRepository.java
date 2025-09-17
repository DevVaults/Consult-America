package consult_america.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import consult_america.demo.model.SentEmail;

@Repository
public interface SentEmailRepository extends JpaRepository<SentEmail, Long> {
    // Additional query methods can be added if needed
}
