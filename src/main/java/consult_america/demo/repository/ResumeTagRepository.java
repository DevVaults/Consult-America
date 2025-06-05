package consult_america.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import consult_america.demo.model.ResumeTag;

public interface ResumeTagRepository extends JpaRepository<ResumeTag, Long> {
}