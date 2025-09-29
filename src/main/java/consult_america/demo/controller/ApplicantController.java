package consult_america.demo.controller;

import consult_america.demo.model.Applicant;
import consult_america.demo.service.ApplicantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applicants")
public class ApplicantController {
    private final ApplicantService service;

    public ApplicantController(ApplicantService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Applicant> create(@RequestBody Applicant applicant) {
        Applicant created = service.create(applicant);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public List<Applicant> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Applicant> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Applicant> update(@PathVariable Long id, @RequestBody Applicant applicant) {
        return service.update(id, applicant)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}