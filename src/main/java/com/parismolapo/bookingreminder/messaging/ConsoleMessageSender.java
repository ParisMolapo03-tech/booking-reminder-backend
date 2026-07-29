package com.parismolapo.bookingreminder.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleMessageSender implements MessageSender {

    @Override
    public void send(String toPhoneNumber, String message) {
        log.info("""
                
                ---------- WHATSAPP MESSAGE (SIMULATED) ----------
                TO      : {}
                MESSAGE : {}
                --------------------------------------------------
                """, toPhoneNumber, message);
    }
}