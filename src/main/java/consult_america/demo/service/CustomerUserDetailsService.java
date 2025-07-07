package consult_america.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import consult_america.demo.model.User;
import consult_america.demo.model.UserProfileDTO;
import consult_america.demo.repository.UserRepository;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        consult_america.demo.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        System.out.println("USER ROLE CHECKER  " + org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .build());

        return org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .build();
    }

    public Optional<consult_america.demo.model.User> getProfileByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveOrUpdateProfile(UserProfileDTO dto) {
        User profile = userRepository.findByEmail(dto.getEmail()).orElse(new User());
        profile.setEmail(dto.getEmail());
        profile.setLastName(dto.getName());
        profile.setClientName1(dto.getClientName1());
        profile.setClientName2(dto.getClientName2());
        profile.setClientName3(dto.getClientName3());
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPrimaryAddress(dto.getPrimaryAddress());
        profile.setPrimaryPhone(dto.getPrimaryPhone());
        profile.setSecondaryAddress(dto.getSecondaryAddress());
        profile.setSecondaryPhone(dto.getSecondaryPhone());
        profile.setTechStack(dto.getTechStack());
        profile.setYearsOfExperience(dto.getYearsOfExperience());
        profile.setEmploymentType(dto.getEmploymentType());
        profile.setWorkAuthorization(dto.getWorkAuthorization());
        profile.setVisaStatus(dto.getVisaStatus());

        return userRepository.save(profile);
    }
}
