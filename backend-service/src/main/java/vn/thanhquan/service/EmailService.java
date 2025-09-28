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

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    private final SendGrid sendGrid;

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
        log.info("Email verification started");

        // Define sender and recipient
        Email fromEmail = new Email(from, "Tây Java");
        Email toEmail = new Email(to);

        // Define email subject
        String subject = "Xác thực tài khoản";

        // Initialize verification link (this should be generated and passed in)
        String secretCode = String.format("?secretCode=xyz", UUID.randomUUID());

        // TODO generate secretCode and save to database

        // Define dynamic data for the template
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("verification_link", verificationLink + "/?secretCode=xyz");

        // Create a new Mail object
        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);

        // Personalize the email for the recipient
        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);

        // Add dynamic data to the personalization object
        map.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);

        // Set the SendGrid Template ID
        mail.setTemplateId(templateId);

        // Create the API request
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        // Send the request and handle the response
        Response response = sendGrid.api(request);
        if (response.getStatusCode() == 202) {
            log.info("Verification sent successfully");
        } else {
            log.error("Verification sent failed");
        }
    }
}
