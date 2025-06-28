package consult_america.demo.controller;

import consult_america.demo.model.Job;
import consult_america.demo.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @PostMapping("/post")
    public ResponseEntity<?> postJob(@RequestBody Job job, Authentication auth) {
        job.setPostedAt(LocalDateTime.now());
        job.setPostedBy(auth.getName());
        Job saved = jobRepository.save(job);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        Optional<Job> job = jobRepository.findById(id);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<?> deleteJobById(@PathVariable Long id) {
    //     Optional<Job> job = jobRepository.findById(id);
    //      jobRepository.delete(job);
    //     return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    // }
}
