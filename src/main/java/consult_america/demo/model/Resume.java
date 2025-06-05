package consult_america.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;              // Candidate name
    private String email;             // Candidate email
    private String contact;           // Contact information

    private String fileName;          // Uploaded file name
    private String fileType;          // MIME type
    private Long fileSize;            // File size in bytes
 private String getSummary;
 
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;              // Resume file content

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime uploadedAt;

    // === Getters & Setters ===


    @ElementCollection
@CollectionTable(name = "resume_tags", joinColumns = @JoinColumn(name = "resume_id"))
@Column(name = "tag")
private List<String> tags = new ArrayList<>();

// Add getters and setters
public List<String> getTags() {
    return tags;
}

public void setTags(List<String> tags) {
    this.tags = tags;
}


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Optional<String> getFileType() {
        return Optional.ofNullable(fileType);
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

  

    public LocalDateTime getUploadedAt() {
    return uploadedAt;
}

   public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
}

    public String getSummary() {
        return getSummary;
    }

    public void setSummary(String getSummary) {
        this.getSummary = getSummary;
    }
}
