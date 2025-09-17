package consult_america.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consult_america.demo.model.SentEmail;
import consult_america.demo.service.SentEmailService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sent-emails")
public class SentEmailController {

    private final SentEmailService sentEmailService;

    public SentEmailController(SentEmailService sentEmailService) {
        this.sentEmailService = sentEmailService;
    }

    @PostMapping
    public ResponseEntity<?> saveSentEmail(@RequestBody SentEmail email) {
        // if client didn't provide date, set server-side timestamp
        if (email.getDate() == null) {
            email.setDate(LocalDateTime.now());
        }

        SentEmail saved = sentEmailService.save(email);
        return ResponseEntity.ok(saved);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<List<SentEmail>> getAll() {
        return ResponseEntity.ok(sentEmailService.findAll());
    }
}
