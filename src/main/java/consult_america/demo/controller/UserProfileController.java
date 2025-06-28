package consult_america.demo.controller;

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

@RestController
@RequestMapping("/user-profile")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserProfileController {

    private final CustomerUserDetailsService service;

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
}
