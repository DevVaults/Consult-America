package consult_america.demo.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import consult_america.demo.model.User;
import consult_america.demo.repository.DocumentUploadRepository;
import consult_america.demo.repository.UserRepository;
import consult_america.demo.model.DocumentUpload;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentUploadController {

    @Autowired
    private DocumentUploadRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("documentType") String documentType
    ) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            DocumentUpload doc = new DocumentUpload();
            doc.setFileName(documentType);
            doc.setFileType(file.getContentType());
            doc.setData(file.getBytes());
            doc.setUser(userOpt.get());
            doc.setUploadedAt(LocalDateTime.now());

            documentRepository.save(doc);
            return ResponseEntity.ok("Document uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentUpload>> getDocumentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(documentRepository.findByUserId(userId));
    }

    // ✅ DELETE Document by ID
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }

        documentRepository.deleteById(documentId);
        return ResponseEntity.ok("Document deleted successfully.");
    }

    // ✅ DOWNLOAD Document by ID
    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long documentId) throws IOException {
        Optional<DocumentUpload> optionalDoc = documentRepository.findById(documentId);

        if (!optionalDoc.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DocumentUpload doc = optionalDoc.get();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + doc.getFileName() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(new InputStreamResource(new ByteArrayInputStream(doc.getData())));
    }
}
