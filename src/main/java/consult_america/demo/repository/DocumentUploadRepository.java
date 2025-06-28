package consult_america.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import consult_america.demo.model.DocumentUpload;

@Repository
public interface DocumentUploadRepository extends JpaRepository<DocumentUpload, Long> {
    // query methods if any

    List<DocumentUpload> findByUserId(Long userId);
}
