package consult_america.demo.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import consult_america.demo.model.Resume;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserId(Long userId);
}