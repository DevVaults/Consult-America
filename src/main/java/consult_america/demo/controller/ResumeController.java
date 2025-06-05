package consult_america.demo.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import consult_america.demo.model.Resume;
import consult_america.demo.model.ResumeDTO;
import consult_america.demo.service.EmailService;
import consult_america.demo.service.ResumeService;
import consult_america.demo.service.ResumeTagExtractionService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeTagExtractionService tagExtractionService;
     private final EmailService emailService;
    // private final FileTextExtractor fileExtractor;

    @Autowired
    public ResumeController(ResumeService resumeService,
                          ResumeTagExtractionService tagExtractionService,EmailService emailService
                          /*, FileTextExtractor fileExtractor*/) {
        this.resumeService = resumeService;
        this.tagExtractionService = tagExtractionService;
        this.emailService = emailService;
        // this.fileExtractor = fileExtractor;
    }

    @PostMapping("/upload") 
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam(value = "summary", required = false) String summary) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Extract text from file
            // String resumeText = FileEditor.extractText(file);
            //String resumeText = ; // TODO: Implement text extraction logic or use a valid utility

            // Extract tags from text
            List<String> tags = tagExtractionService.extractTagsFromText(summary);

            // Create and save resume
            Resume resume = new Resume();
            resume.setName(name);
            resume.setEmail(email);
            resume.setContact(contact);
            resume.setFileName(file.getOriginalFilename());
            resume.setFileType(file.getContentType());
            resume.setFileSize(file.getSize());
            resume.setData(file.getBytes());
            resume.setUploadedAt(LocalDateTime.now());
            resume.setSummary(summary); 
            resume.setTags(tags);

            Resume savedResume = resumeService.saveResume(resume);

            // Build response DTO
            ResumeDTO resumeDTO = mapToResumeDTO(savedResume);

            // Add download URL
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/resumes/")
                    .path(savedResume.getId().toString())
                    .path("/download")
                    .toUriString();

            Map<String, Object> response = new HashMap<>();
            response.put("resume", resumeDTO);
            response.put("downloadUrl", downloadUrl);
            response.put("tags", tags);
            response.put("message", "Resume uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading resume: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
public ResponseEntity<Map<String, Object>> getStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalResumes", resumeService.getTotalResumes());
    stats.put("weeklyUploads", resumeService.getUploadsThisWeek());
    stats.put("storageUsedMB", resumeService.getStorageUsedInMB());
    return ResponseEntity.ok(stats);
}

    @GetMapping
    public ResponseEntity<Page<ResumeDTO>> getAllResumes(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search) {

        Page<Resume> resumes = search != null ?
                resumeService.searchResumes(search, pageable) :
                resumeService.getAllResumes(pageable);

        Page<ResumeDTO> resumeDTOs = resumes.map(this::mapToResumeDTO);
        return ResponseEntity.ok(resumeDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDTO> getResumeById(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .map(this::mapToResumeDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeDTO>> getResumesByUser(@PathVariable Long userId) {
        List<Resume> resumes = resumeService.getResumesByUserId(userId);
        List<ResumeDTO> resumeDTOs = resumes.stream()
                .map(this::mapToResumeDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resumeDTOs);
    }

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

    @GetMapping("/{id}/view")
    public ResponseEntity<ByteArrayResource> viewResume(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .map(resume -> {
                    ByteArrayResource resource = new ByteArrayResource(resume.getData());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + resume.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(resume.getFileType().orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                            .contentLength(resume.getFileSize())
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tags")
    public ResponseEntity<List<String>> getResumeTags(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .map(Resume::getTags)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable Long id) {
        try {
            resumeService.deleteResume(id);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Resume deleted successfully",
                    "deletedId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting resume: " + e.getMessage());
        }
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

  @PostMapping("/{id}/send-profile")
public ResponseEntity<?> sendProfileEmail(
        @PathVariable Long id,
        @RequestParam String recipientEmail,
        @RequestParam String subject,
        @RequestParam(required = false) String customMessage) {

    return resumeService.getResumeById(id)
        .map(resume -> {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("subject", subject);
                variables.put("candidate", Map.of(
                    "name", resume.getName(),
                    "email", resume.getEmail(),
                    "contact", resume.getContact(),
                    "summary", resume.getSummary() != null && !resume.getSummary().isBlank()
                        ? resume.getSummary()
                        : customMessage,
                    "tags", resume.getTags()
                ));

                // Create a temporary file for resume attachment
                File tempFile = File.createTempFile("resume-", "-" + resume.getFileName());
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(resume.getData());
                }

                emailService.sendProfileEmail(
                    recipientEmail,
                    subject,
                    variables,
                    tempFile
                );

                return ResponseEntity.ok(Map.of(
                    "message", "Email sent successfully",
                    "to", recipientEmail
                ));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Failed to send email",
                        "details", e.getMessage()
                    ));
            }
        })
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Resume not found with ID: " + id)));
}

}