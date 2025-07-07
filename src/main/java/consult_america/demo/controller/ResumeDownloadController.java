package consult_america.demo.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consult_america.demo.repository.UserRepository;
import consult_america.demo.service.EmailService;
import consult_america.demo.service.ResumeService;
import consult_america.demo.service.ResumeTagExtractionService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/resumes")
public class ResumeDownloadController {

    private final ResumeService resumeService;

    public ResumeDownloadController(
            ResumeService resumeService,
            ResumeTagExtractionService tagExtractionService,
            EmailService emailService,
            UserRepository userRepository) {
        this.resumeService = resumeService;
    }

    // Example usage to avoid "never read" and "not used" errors
    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .map(resume -> {
                    ByteArrayResource resource = new ByteArrayResource(resume.getData());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + resume.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(resume.getFileType().orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                            .contentLength(resume.getFileSize())
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
