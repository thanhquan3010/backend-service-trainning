package vn.thanhquan.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    private final SendGrid sendGrid;
    private final UserRepository userRepository;

    @Value("${spring.sendGrid.fromEmail}")
    private String from;

    @Value("${spring.sendGrid.templateId}")
    private String templateId;

    @Value("${spring.sendGrid.verificationLink}")
    private String verificationLink;

    public void send(String to, String subject, String text) {
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Content content = new Content("text/plain", text);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() == 202) {
                log.info("Email sent successfully");
            } else {
                log.error("Email sent failed");
            }
        } catch (IOException e) {
            log.error("Email sent failed, errorMessage={}", e.getMessage());
        }
    }

    /**
     * Email verification by Sendgrid
     * 
     * @param to
     * @param name
     * @throws IOException
     */
    public void emailVerification(String to, String name) throws IOException {
        log.info("Email verification started for {}", to);

        UserEntity user = userRepository.findByUsername(name)
                .orElseThrow(() -> new RuntimeException("User not found: " + name));

        String secretCode = UUID.randomUUID().toString();
        user.setVerificationCode(secretCode);
        userRepository.save(user);

        Email fromEmail = new Email(from, "Tây Java");
        Email toEmail = new Email(to);
        String subject = "Xác thực tài khoản";
        String verificationUrl = verificationLink + "?secretCode=" + secretCode;

        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("verification_link", verificationUrl);

        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);
        map.forEach(personalization::addDynamicTemplateData);
        mail.addPersonalization(personalization);

        mail.setTemplateId(templateId);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        if (response.getStatusCode() == 202) {
            log.info("Verification email sent successfully to {}", to);
        } else {
            log.error("Failed to send verification email to {}", to);
        }
    }
}
