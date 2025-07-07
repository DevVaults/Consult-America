package consult_america.demo.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public void sendResetPasswordEmail(String toEmail, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Password Reset Request");
        helper.setText(
                "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to reset your password:</p>"
                + "<p><a href=\"" + resetLink + "\">Reset My Password</a></p>"
                + "<br><p>If you did not request this, you can ignore this email.</p>",
                true // enable HTML
        );

        mailSender.send(message);
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

    public void sendCandidateEmail(String to, String subject, Map<String, Object> variables, List<File> attachments)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("candidate-profile", context);

        helper.setFrom("hannankaimkhani96@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // Attach multiple files if any
        if (attachments != null) {
            for (int i = 0; i < attachments.size(); i++) {
                File file = attachments.get(i);
                if (file != null && file.exists()) {
                    FileSystemResource resource = new FileSystemResource(file);
                    // Name attachments like: Resume1.pdf, Document2.pdf etc.
                    helper.addAttachment(file.getName(), resource);
                }
            }
        }

        mailSender.send(message);
    }

    public void sendPlainEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hr@consultamerica.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}
