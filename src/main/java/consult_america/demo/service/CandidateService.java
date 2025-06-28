package consult_america.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import consult_america.demo.model.User;
import consult_america.demo.repository.UserRepository;

@Service
public class CandidateService {

    @Autowired
    private UserRepository candidateRepository;

    public List<User> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Optional<User> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }

    public User saveCandidate(User candidate) {
        return candidateRepository.save(candidate);
    }

    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }
}
