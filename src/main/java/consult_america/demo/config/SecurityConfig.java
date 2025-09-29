package consult_america.demo.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import consult_america.demo.service.CustomerUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomerUserDetailsService userDetailsService;

    public SecurityConfig(CustomerUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // ✅ CSRF disabled for REST
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // ✅ Public login/register API
                .requestMatchers("/resumes/**").authenticated()
                .requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/user-profile/**").permitAll()
                .requestMatchers("/jobs/**").permitAll()
                .requestMatchers("/api/documents/**").permitAll()
                .requestMatchers("api/resumes/**").permitAll()
                .requestMatchers("candidates/**").permitAll()
                .requestMatchers("/applicants/**").permitAll() // ✅ Applicants can manage their profiles
                  // Candidates can view jobs
                .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable()) // ✅ Disable default login form
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(csrf -> csrf.disable());

        //         "HTTP. BUILD CHCKER =  "
        //         + http.build()
        // );
        return http.build();
    }

    // ✅ Expose AuthenticationManager for custom login endpoint
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ✅ Allow Angular app to access APIs (CORS)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
