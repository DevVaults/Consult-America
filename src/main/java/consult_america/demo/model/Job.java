package consult_america.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String location;
    private String employmentType; // e.g. Full-time, Part-time
    private String salaryRange;
    private String contactEmail;

    private LocalDateTime postedAt;
    private String clientName;

    private String postedBy; // Admin email or name
    private String technologyStack;

    // Constructors
    public Job() {
    }

    public Job(Long id, String title, String description, String location, String employmentType,
            String salaryRange, String contactEmail, LocalDateTime postedAt, String postedBy, String clientName, String technologyStack) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.employmentType = employmentType;
        this.salaryRange = salaryRange;
        this.contactEmail = contactEmail;
        this.postedAt = postedAt;
        this.postedBy = postedBy;
        this.clientName = clientName;
        this.technologyStack = technologyStack;
    }

    // Getters and setters
    // ... (generate via IDE or Lombok)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }

    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }
}
