package consult_america.demo.config;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;


@Configuration
public class ThymeleafConfig {

    @Bean
    public ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }
    @Bean
    public org.thymeleaf.spring5.SpringTemplateEngine emailTemplateEngine() {
        org.thymeleaf.spring5.SpringTemplateEngine engine = new org.thymeleaf.spring5.SpringTemplateEngine();
        engine.setTemplateResolver(emailTemplateResolver());
        return engine;
    }

    @Bean
    public org.thymeleaf.templateresolver.ClassLoaderTemplateResolver emailTemplateResolver() {
        org.thymeleaf.templateresolver.ClassLoaderTemplateResolver templateResolver = new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/email/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
