package consult_america.demo.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consult_america.demo.model.Role;
import consult_america.demo.model.User;
import consult_america.demo.repository.RoleRepository;
import consult_america.demo.repository.UserRepository;
import consult_america.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow CORS for Angular frontend
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(user.getRole().getName()).orElse(null);
        if (role == null) {
            return ResponseEntity.badRequest().body("Invalid role");
        }

        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("Success", "User is Created Successfully"));
    }

    // @PostMapping("/login")
    // public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    //     try {
    //         Authentication auth = authenticationManager.authenticate(
    //                 new UsernamePasswordAuthenticationToken(
    //                         credentials.get("email"),
    //                         credentials.get("password")
    //                 )
    //         );
    //         UserDetails userDetails = (UserDetails) auth.getPrincipal();
    //         consult_america.demo.model.User dbUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    //         return ResponseEntity.ok(Map.of(
    //                 "message", "Login successful",
    //                 "userId", dbUser.getId(),
    //                 "email", dbUser.getEmail(),
    //                 "role", dbUser.getRole().getName()
    //         ));
    //     } catch (AuthenticationException e) {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                 .body(Map.of("error", "Invalid email or password"));
    //     }
    // }
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> credentials,
            HttpServletRequest request) {

        try {
            // Authenticate the user using Spring Security
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.get("email"),
                            credentials.get("password")
                    )
            );

            // 1. Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 2. Manually create HTTP session and store the security context
            HttpSession session = request.getSession(true); // creates session if not exists
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            // Get user info from DB
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            consult_america.demo.model.User dbUser = userRepository
                    .findByEmail(userDetails.getUsername())
                    .orElseThrow();

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "userId", dbUser.getId(),
                    "email", dbUser.getEmail(),
                    "role", dbUser.getRole().getName()
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false); // false = do not create if doesn't exist
            if (session != null) {
                session.invalidate(); // Only invalidate if session exists
            }
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed", "details", e.getMessage()));
        }
    }

    // === Forgot Password Endpoint ===
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No user found with that email.");
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        try {
            emailService.sendResetPasswordEmail(email, resetLink);
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Error sending email.");
        }

        return ResponseEntity.ok("Reset link sent to your email.");
    }

    // === Reset Password Endpoint ===
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        System.out.println("Received token: " + token);
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid reset token.");
        }

        User user = userOpt.get();
        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
    }

}
