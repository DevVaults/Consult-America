package consult_america.demo.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

@RestController
@RequestMapping("/employee/resumes")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE')")
public class EmployeeResumeController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private ResumeTagExtractionService tagExtractionService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/me")
    public ResponseEntity<List<ResumeDTO>> getMyResumes(Authentication auth) {
        List<Resume> resumes = resumeService.getMyResume(auth.getName());
        List<ResumeDTO> resumeDTOs = resumes.stream().map(resumeService::toDto).toList();
        return ResponseEntity.ok(resumeDTOs);
    }

}
