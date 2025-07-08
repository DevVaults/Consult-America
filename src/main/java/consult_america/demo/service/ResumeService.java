package consult_america.demo.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable; // ✅ CORRECT

import consult_america.demo.model.Resume;
import consult_america.demo.model.ResumeDTO;
import consult_america.demo.model.User;
import consult_america.demo.repository.ResumeRepository;
import consult_america.demo.repository.UserRepository;
import jakarta.mail.MessagingException;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    private final EmailService emailService;

    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository, EmailService emailService) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public Page<Resume> getAllResumes(Pageable pageable) {
        return resumeRepository.findAll(pageable);
    }

    public Page<Resume> searchResumesByUploader(String email, Pageable pageable) {
        return resumeRepository.findByUploadedBy(email, pageable);
    }

    public Page<Resume> getResumesByUploader(String email, String keyword, Pageable pageable) {
        return resumeRepository.searchByUploaderAndKeyword(email, keyword, pageable);
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

    public ResumeDTO toDto(Resume resume) {
        ResumeDTO dto = new ResumeDTO();
        dto.setId(resume.getId());
        dto.setName(resume.getName());
        dto.setEmail(resume.getEmail());
        dto.setContact(resume.getContact());
        dto.setUploadedAt(resume.getUploadedAt());
        dto.setFileName(resume.getFileName());
        dto.setFileType(resume.getFileType().orElse(null));
        dto.setFileSize(resume.getFileSize());
        dto.setSummary(resume.getSummary());
        dto.setTags(resume.getTags());
        // You can set downloadUrl in controller or here if preferred
        return dto;
    }

    // 2. Get resumes by username (used in controller)
    public List<Resume> getResumesByUsername(String username) {
        return resumeRepository.findByName(username);
    }

    public List<Resume> getResumeByUserId(Long userId) {
        return resumeRepository.findByUserId(userId);

    }

    public List<Resume> getResume(String email) {
        return resumeRepository.findByEmail(email);
    }

    public List<Resume> getMyResume(String usernameOrEmail) {
        Optional<User> user = userRepository.findByEmail(usernameOrEmail);
        if (user.isPresent()) {
            return resumeRepository.findByUploadedBy(usernameOrEmail);
        } else {
            return new ArrayList<>();
        }
    }

    // 3. Check if the given user is the owner of the resume by id and username
    public boolean isOwner(Long resumeId, String username) {
        Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
        return resumeOpt.map(resume
                -> resume.getUser() != null && resume.getUser().getFirstName().equals(username)
        ).orElse(false);
    }

    public List<Resume> getResumesByEmail(String email) {
        return resumeRepository.findByEmailIgnoreCase(email.trim());
    }

    public void sendResumeProfileEmail(Long resumeId, String recipientEmail, String subject, String customMessage, String userEmail)
            throws IOException, MessagingException {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        // try {
        //     User user = userRepository.findByEmail(userEmail)
        //             .orElseThrow(() -> new RuntimeException("Resume not found"));
        // } catch (RuntimeException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Associated user not found"));

        emailService.sendProfileEmailWithResume(user, resume, recipientEmail, subject, customMessage);
    }

}
