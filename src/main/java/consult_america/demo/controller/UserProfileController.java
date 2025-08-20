package consult_america.demo.controller;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consult_america.demo.model.User;
import consult_america.demo.model.UserProfileDTO;
import consult_america.demo.service.CustomerUserDetailsService;
import jakarta.mail.MessagingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import consult_america.demo.service.EmailService;
import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/user-profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final CustomerUserDetailsService service;

    @Autowired
    private EmailService emailService;

    public UserProfileController(CustomerUserDetailsService service) {
        this.service = service;
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getProfile(@PathVariable String email) {
        return service.getProfileByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDTO dto) {
        User saved = service.saveOrUpdateProfile(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/send-email-complete")
    public ResponseEntity<?> sendCandidateEmail(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        try {
            Map<String, Object> infoVars = (Map<String, Object>) payload.get("info");

            if (infoVars == null || infoVars.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Candidate info must be provided.");
            }

            String toEmail = (String) infoVars.get("email");
            if (toEmail == null || toEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Candidate email is required in info.");
            }

            String subject = "Candidate Profile Information";
            List<File> attachments = new ArrayList<>();

            // ✅ Download resumes from provided URLs
            if (payload.containsKey("resumes")) {
                List<Map<String, String>> resumes = (List<Map<String, String>>) payload.get("resumes");
                for (Map<String, String> r : resumes) {
                    String urlStr = r.get("url");
                    String fileName = r.get("fileName");
                    File file = downloadTempFile(urlStr, fileName);
                    if (file != null) {
                        attachments.add(file);
                    }
                }
            }

            // ✅ Download documents from provided URLs
            if (payload.containsKey("documents")) {
                List<Map<String, String>> documents = (List<Map<String, String>>) payload.get("documents");
                for (Map<String, String> d : documents) {
                    String urlStr = d.get("url");
                    String fileName = d.get("fileName");
                    File file = downloadTempFile(urlStr, fileName);
                    if (file != null) {
                        attachments.add(file);
                    }
                }
            }

            // ✅ Send email
            emailService.sendCandidateEmail(toEmail, subject, infoVars, attachments);

            return ResponseEntity.ok(Map.of("message", "Email has been reset successfully."));

        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request: " + ex.getMessage());
        }
    }

    /**
     * Helper: Downloads file from URL and saves it temporarily.
     */
    private File downloadTempFile(String urlStr, String fileName) {
        try (InputStream in = new URL(urlStr).openStream()) {
            File tempFile = File.createTempFile("attach-", "-" + fileName);
            try (OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }
            return tempFile;
        } catch (Exception e) {
            System.err.println("Failed to download: " + urlStr + " → " + e.getMessage());
            return null;
        }
    }

}
