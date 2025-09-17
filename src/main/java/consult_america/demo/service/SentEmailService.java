package consult_america.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import consult_america.demo.model.SentEmail;
import consult_america.demo.repository.SentEmailRepository;

@Service
public class SentEmailService {

    private final SentEmailRepository repository;

    public SentEmailService(SentEmailRepository repository) {
        this.repository = repository;
    }

    public SentEmail save(SentEmail email) {
        return repository.save(email);
    }

    public List<SentEmail> findAll() {
        return repository.findAll();
    }
}
