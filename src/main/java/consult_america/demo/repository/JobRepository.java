package consult_america.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import consult_america.demo.model.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

}
