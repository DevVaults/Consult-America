package consult_america.demo.controller;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

import org.springframework.web.bind.annotation.CrossOrigin;
import consult_america.demo.model.User;
import consult_america.demo.service.CandidateService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private ResumeTagExtractionService tagExtractionService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private CandidateService candidateService;

    @GetMapping("/candidates")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
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
        dto.setVisaStatus(resume.getVisaStatus());
        dto.setLinkedln(resume.getlinkedln());

        // Set download URL
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/resumes/")
                .path(resume.getId().toString())
                .path("/download")
                .toUriString();
        dto.setDownloadUrl(downloadUrl);

        return dto;
    }

    @GetMapping("/candidates/{id}")
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

@PostMapping("/update/{id}")
public ResponseEntity<?> updateResume(
        @PathVariable Long id,
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam("name") String name,
        @RequestParam(value = "email", required = false) String email,
        @RequestParam(value = "contact", required = false) String contact,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "summary", required = false) String summary,
        @RequestParam(value = "visaStatus", required = false) String visaStatus,
        @RequestParam(value = "linkedln", required = false) String linkedln
) {
    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        // ✅ Check if resume exists
        Resume existingResume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + id));

        // ✅ Update only fields that are provided
        if (name != null) existingResume.setName(name);
        if (email != null) existingResume.setEmail(email);
        if (contact != null) existingResume.setContact(contact);
        if (title != null) existingResume.setTitle(title);
        if (summary != null) {
            existingResume.setSummary(summary);
            List<String> tags = tagExtractionService.extractTagsFromText(summary);
            existingResume.setTags(tags);
        }
        if (visaStatus != null) existingResume.setVisaStatus(visaStatus);
        if (linkedln != null) existingResume.setlinkedln(linkedln);

        // ✅ If a new file is uploaded, replace it
        if (file != null && !file.isEmpty()) {
            existingResume.setFileName(file.getOriginalFilename());
            existingResume.setFileType(file.getContentType());
            existingResume.setFileSize(file.getSize());
            existingResume.setData(file.getBytes());
        }

        //existingResume.setUpdatedAt(LocalDateTime.now()); // add updated timestamp
        existingResume.setUploadedBy(auth.getName());     // track who updated it

        Resume updatedResume = resumeService.saveResume(existingResume);

        // Build DTO for response
        ResumeDTO resumeDTO = mapToResumeDTO(updatedResume);
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/employee/resumes/")
                .path(updatedResume.getId().toString())
                .path("/download")
                .toUriString();

        Map<String, Object> response = new HashMap<>();
        response.put("resume", resumeDTO);
        response.put("downloadUrl", downloadUrl);
        response.put("message", "Resume updated successfully");

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating resume: " + e.getMessage());
    }
}


    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam(value = "visaStatus", required = false) String visaStatus,
            @RequestParam(value = "linkedln", required = false) String linkedln
    ) {

        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // ✅ manual fetch
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            List<String> tags = tagExtractionService.extractTagsFromText(summary);

            Resume resume;
            resume = new Resume();
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
            resume.setUploadedBy(auth.getName());
            resume.setTitle(title);
            resume.setVisaStatus(visaStatus);
            resume.setlinkedln(linkedln);
            System.out.println("visaStatus data: " + visaStatus);
            System.out.println("linkedln data: " + linkedln);
            Resume savedResume = resumeService.saveResume(resume);

            // Build response DTO
            ResumeDTO resumeDTO = mapToResumeDTO(savedResume);

            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/employee/resumes/")
                    .path(savedResume.getId().toString())
                    .path("/download")
                    .toUriString();

            Map<String, Object> response = new HashMap<>();
            response.put("resume", resumeDTO);
            response.put("downloadUrl", downloadUrl);
            response.put("tags", tags);
            response.put("message", "Resume uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading resume: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMyResume(@PathVariable Long id, Authentication auth) {
        if (resumeService.isOwner(id, auth.getName())) {
            resumeService.deleteResume(id);
            return ResponseEntity.ok(Map.of("message", "Deleted", "id", id));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this resume");
        }
    }

    @PostMapping("/{id}/send-profile")
    public ResponseEntity<?> sendProfileEmail(
            @PathVariable Long id,
            @RequestParam String recipientEmail,
            @RequestParam String subject,
            @RequestParam(required = false) String customMessage,
            Authentication auth) {

        return resumeService.getResumeById(id)
                .map(resume -> {
                    if (!resume.getUploadedBy().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("You are not authorized to email this resume");
                    }
                    try {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("subject", subject);
                        variables.put("candidate", Map.of(
                                "name", resume.getName(),
                                "email", resume.getEmail(),
                                "contact", resume.getContact(),
                                "summary", resume.getSummary() != null ? resume.getSummary() : customMessage,
                                "tags", resume.getTags()
                        ));

                        File tempFile = File.createTempFile("resume-", "-" + resume.getFileName());
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            fos.write(resume.getData());
                        }

                        emailService.sendProfileEmail(recipientEmail, subject, variables, tempFile,resume.getEmail());
                        return ResponseEntity.ok(Map.of("message", "Email sent", "to", recipientEmail));

                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to send email", "details", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Resume not found with ID: " + id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable Long id, Authentication auth) {
        return resumeService.getResumeById(id)
                .filter(resume -> resume.getUploadedBy().equals(auth.getName()))
                .map(resume -> {
                    ByteArrayResource resource = new ByteArrayResource(resume.getData());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(resume.getFileType().orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                            .contentLength(resume.getFileSize())
                            .body(resource);
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
}
