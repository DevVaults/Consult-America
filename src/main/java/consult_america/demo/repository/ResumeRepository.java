package consult_america.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import consult_america.demo.model.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserId(Long userId);

    List<Resume> findByName(String name);

    List<Resume> findByUploadedBy(String email);

    List<Resume> findByEmail(String email);

    List<Resume> findByEmailIgnoreCase(String email);

    Page<Resume> findByUploadedBy(String uploadedBy, Pageable pageable);

    @Query("SELECT r FROM Resume r WHERE r.uploadedBy = :email AND "
            + "(LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(r.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Resume> searchByUploaderAndKeyword(@Param("email") String email,
            @Param("keyword") String keyword,
            Pageable pageable);
}
