package consult_america.demo.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import consult_america.demo.model.Resume;
import consult_america.demo.model.ResumeDTO;
import consult_america.demo.model.User;
import consult_america.demo.service.CandidateService;
import consult_america.demo.service.EmailService;
import consult_america.demo.service.ResumeService;
import consult_america.demo.service.ResumeTagExtractionService;

@RestController
@RequestMapping("/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private ResumeTagExtractionService tagExtractionService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private CandidateService candidateService;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    @GetMapping("/{id}")
    public Optional<Object> getById(@PathVariable Long id) {
        return candidateService.getCandidateById(id)
                .map(candidate -> ResponseEntity.ok().body(candidate)); // returns ResponseEntity<Candidate>
        // returns ResponseEntity<String>
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<ResumeDTO>> getResumesByEmail(@PathVariable String email) {
        List<Resume> resumes = resumeService.getResumesByEmail(email);
        System.out.print(resumes);
        List<ResumeDTO> resumeDTOs = resumes.stream()
                .map(this::mapToResumeDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resumeDTOs);
    }

    private ResumeDTO mapToResumeDTO(Resume resume) {
        ResumeDTO dto = new ResumeDTO();
        dto.setId(resume.getId());
        dto.setName(resume.getName());
        dto.setEmail(resume.getEmail());
        dto.setContact(resume.getContact());
        dto.setUploadedAt(resume.getUploadedAt());
        dto.setFileName(resume.getFileName());
        dto.setFileType(resume.getFileType().orElse(null));
        dto.setFileSize(resume.getFileSize());
        dto.setTags(resume.getTags());

        // Set download URL
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/resumes/")
                .path(resume.getId().toString())
                .path("/download")
                .toUriString();
        dto.setDownloadUrl(downloadUrl);

        return dto;
    }

}
