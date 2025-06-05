package consult_america.demo.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ResumeTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tag;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    public Long getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public Resume getResume() {
        return resume;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    
}
