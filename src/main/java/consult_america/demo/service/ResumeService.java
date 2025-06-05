package consult_america.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import consult_america.demo.model.Resume;
import consult_america.demo.model.ResumeDTO;
import consult_america.demo.repository.ResumeRepository;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

   public Page<Resume> getAllResumes(Pageable pageable) {
    return resumeRepository.findAll(pageable);
}

    private ResumeDTO convertToDTO(Resume resume) {
        // Map Resume fields to ResumeDTO fields as appropriate
        ResumeDTO dto = new ResumeDTO();
        dto.setId(resume.getId());
        dto.setId(resume.getId());
        dto.setName(resume.getName());
        dto.setSummary(resume.getSummary());
        // Add other fields as needed
        return dto;
    }

    public List<Resume> getResumesByUserId(Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    public Resume saveResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }

    public Page<Resume> searchResumes(String search, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchResumes'");
    }

    public Object getTotalResumes() {
        // TODO Auto-generated method stub
        return resumeRepository.count();
    }
public double getStorageUsedInMB() {
        return resumeRepository.findAll().stream()
                .mapToDouble(r -> r.getFileSize() / (1024.0 * 1024.0))
                .sum();
    }

    
    public long getUploadsThisWeek() {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
        return resumeRepository.findAll().stream()
                .filter(r -> r.getUploadedAt() != null && !r.getUploadedAt().isBefore(startOfWeekDateTime))
                .count();
    }
    

   
}
