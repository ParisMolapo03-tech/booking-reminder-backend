package com.parismolapo.bookingreminder.messaging;

public interface EmailSender {

    void send(String toEmail, String subject, String body);
}