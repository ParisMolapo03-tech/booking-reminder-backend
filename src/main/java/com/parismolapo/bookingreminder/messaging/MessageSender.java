package com.parismolapo.bookingreminder.messaging;

public interface MessageSender {

    void send(String toPhoneNumber, String message);
}