package com.parismolapo.bookingreminder.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String toEmail, String subject, String body) {
        log.info("""
                
                ---------- EMAIL (SIMULATED) ----------
                TO      : {}
                SUBJECT : {}
                BODY    : {}
                ---------------------------------------
                """, toEmail, subject, body);
    }
}