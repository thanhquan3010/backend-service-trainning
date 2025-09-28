package vn.thanhquan.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.thanhquan.service.EmailService;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-CONTROLLER")
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/send-email")
    public void sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        log.info("Sending email to {}", to);
        emailService.send(to, subject, body); // Lưu ý: tên phương thức có thể là sendEmail tùy vào file EmailService
                                              // của bạn
    }

    @GetMapping("/verify-email")
    public void emailVerification(@RequestParam String to, @RequestParam String name) throws IOException {
        log.info("Verifying email to {}", to);
        emailService.emailVerification(to, name);
    }
}
