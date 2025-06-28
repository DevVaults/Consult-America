package consult_america.demo.model;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeDTO {

    private Long id;
    private String name;
    private String email;
    private String contact;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private List<String> tags;
    private String downloadUrl;
    private String summary;

    // === Constructors ===

    public ResumeDTO() {
    }

    public ResumeDTO(Long id, String name, String email, String contact,
                     String fileName, String fileType, Long fileSize,
                     LocalDateTime uploadedAt, List<String> tags, String summary) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.tags = tags;
        this.summary = summary;
    }

    // === Getters and Setters ===

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }

    public void setContact(String contact) { this.contact = contact; }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }

    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }

    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public String getDownloadUrl() { return downloadUrl; }

    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    @Override
    public String toString() {
        return "ResumeDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedAt=" + uploadedAt +
                ", tags=" + tags +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }
}