package consult_america.demo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class ResumeTagExtractionService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResumeTagExtractionService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.meaningcloud.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> extractTagsFromText(String text) throws IOException {
        // First try MeaningCloud (free tier available)
        List<String> tags = extractWithMeaningCloud(text);
        if (tags.isEmpty()) {
            // Fallback to simple keyword matching
            tags = simpleKeywordMatching(text);
        }
        return tags;
    }

    private List<String> extractWithMeaningCloud(String text) {
        try {
            Mono<String> response = webClient.post()
                    .uri("/topics-2.0")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("key", "your-free-api-key")
                            .with("txt", text)
                            .with("lang", "en"))
                    .retrieve()
                    .bodyToMono(String.class);

            String jsonResponse = response.block();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode topics = root.path("entity_list");

            List<String> tags = new ArrayList<>();
            for (JsonNode topic : topics) {
                tags.add(topic.path("form").asText());
            }
            return tags;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<String> simpleKeywordMatching(String text) {
        Set<String> skills = Set.of(
               "Java", "Python", "JavaScript", "TypeScript", "C#", "C++", "C", "R", "SQL",
"MySQL", "MSSQL", "PostgreSQL", "MongoDB", "Oracle", "Power BI", "Tableau",
"React", "Angular", "Vue", "Node.js", "Spring", "Spring Boot", "WinForms", "WPF", "Flutter", "Android Native",
".NET", "Docker", "Kubernetes", "VMware", "ESXi", "vSphere", "Hyper-V", "Containers",
"AWS", "Azure", "Google Cloud", "GCP", "EC2", "S3", "RDS", "Lambda", "IAM", "VPC", "EKS", "GCE", "GCS", "GKE", "Vertex AI",
"Terraform", "Azure DevOps", "CI/CD", "PowerShell", "Bash", "Git", "GitHub", "GitLab", "Jenkins", "IaC", "SonarQube",
"Apache Kafka", "RabbitMQ", "Azure Service Bus", "Amazon MQ", "PubNub",
"Postman", "Swagger", "API Gateway", "SignalR", "ETL", "Data Integrity", "Master Data Management", "Serialization",
"TCP/IP", "Routing", "Switching", "VPN", "Network Security", "Firewall", "IDS/IPS", "Network Design", "Performance Optimization",
"CISM", "CISSP", "Cloud Security Architecture", "GxP", "DSCSA", "EPCIS", "GS1", "21 CFR Part 11", "CAPA", "SOP Development",
"Validation", "IQ", "OQ", "PQ", "Audit", "Risk Assessment", "Compliance",
"Machine Learning", "Generative AI", "Agile", "Scrum", "Project Management", "TOGAF",
"Communication", "Leadership", "Problem Solving", "Mentorship", "Cloud Governance", "Backup and Disaster Recovery"


        );

        return skills.stream()
                .filter(skill -> text.toLowerCase().contains(skill.toLowerCase()))
                .collect(Collectors.toList());
    }
}
