package consult_america.demo.service;

import consult_america.demo.model.Applicant;
import consult_america.demo.repository.ApplicantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicantService {
    private final ApplicantRepository repository;

    public ApplicantService(ApplicantRepository repository) {
        this.repository = repository;
    }

    public Applicant create(Applicant applicant) {
        return repository.save(applicant);
    }

    public List<Applicant> getAll() {
        return repository.findAll();
    }

    public Optional<Applicant> getById(Long id) {
        return repository.findById(id);
    }

    public Optional<Applicant> update(Long id, Applicant updated) {
        return repository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setMiddleName(updated.getMiddleName());
            existing.setLastName(updated.getLastName());
            existing.setVisaStatus(updated.getVisaStatus());
            existing.setDocuments(updated.getDocuments());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}