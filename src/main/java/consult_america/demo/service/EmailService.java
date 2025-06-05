package consult_america.demo.service;

import java.io.File;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {


    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, 
                       SpringTemplateEngine emailTemplateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = emailTemplateEngine;
    }

   public void sendProfileEmail(String to, String subject,
                             Map<String, Object> variables,
                             File pdfResumeFile) throws MessagingException {
    
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // 'true' = multipart

    Context context = new Context();
    context.setVariables(variables);

    String htmlContent = templateEngine.process("profile-summary", context);

    helper.setFrom("hannankaimkhani96@gmail.com"); // Gmail username must match authenticated user
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    // ✅ Attach PDF resume
    if (pdfResumeFile != null && pdfResumeFile.exists()) {
        FileSystemResource file = new FileSystemResource(pdfResumeFile);
        helper.addAttachment("Resume.pdf", file);
    }

    mailSender.send(message);
}

}